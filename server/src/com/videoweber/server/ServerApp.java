package com.videoweber.server;

import com.videoweber.lib.app.App;
import com.videoweber.lib.app.AppLockManager;
import com.videoweber.lib.app.UndeliveredMessageException;
import com.videoweber.server.service.AppStateHolderService;
import com.videoweber.lib.app.service.ParametersService;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.gui.GuiInitTimeoutException;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.SchedulerService;
import com.videoweber.lib.app.service.TempDirService;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.service.communicator_service.CommunicatorService;
import com.videoweber.server.service.storage_service.StorageService;
import com.videoweber.server.window.main.MainController;
import com.videoweber.server.window.main.MainWindow;
import it.sauronsoftware.junique.AlreadyLockedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ServerApp extends App {

    private static final Logger LOG = Logger.getLogger(ServerApp.class.getName());
    /**
     * [1, ...] the code is completely rewritten
     *
     * [0, ...] significant changes
     *
     * 0 - alpha, 1 - beta, 2 - release candidate, 3 - public release
     *
     * [0, ...] any changes
     */
    public static final String VERSION = "1.1.0.13";
    private static ServiceContainer serviceContainer = null;
    private static Thread guiThread = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            try {
                if (args[0].equals("-version")) {
                    System.out.println(VERSION);
                    return;
                }
            } catch (Exception e) {
                // Ignoring
            }

            ServerApp app = new ServerApp();
            app.initInstance(app);

            // Logging is the first thing we need.
//            app.initConsoleLoggin();
            /**
             * Now we need some GUI tools for alerts. App is starting but not
             * running yet.
             */
            app.initGui();

            // We may need parameters here so it's time of ServiceContainer.
            serviceContainer = new ServiceContainer();

            // Splashskreen demonstration.
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
            // Lock checking.
            app.lockChecking();

            // Clear old tmp files.
            serviceContainer.getService(TempDirService.class);

            // Checking DB connection.
            serviceContainer.getService(HibernateService.class).acquireSession((session) -> {
            });

            // Running the business logic.
            ServerGui.runApp();

            serviceContainer.getService(WindowService.class).openWindow(MainWindow.class);

            // Sheduler
            serviceContainer.getService(SchedulerService.class)
                    .schedule(() -> {
                        SampleRepository sampleRepository = (SampleRepository) serviceContainer
                                .getService(HibernateService.class)
                                .getRepository(SampleEntity.class);
                        sampleRepository.markOldUnrecordedAsDeleted();
                    }, 10 * 60);
            serviceContainer.getService(SchedulerService.class)
                    .schedule(() -> {
                        serviceContainer
                                .getService(StorageService.class)
                                .cleanupDeleted();
                    }, 10 * 60);
            serviceContainer.getService(SchedulerService.class)
                    .schedule(() -> {
                        serviceContainer
                                .getService(StorageService.class)
                                .allocateSpace(60 * 60);
                    }, 10 * 60);
            serviceContainer.getService(SchedulerService.class)
                    .schedule(() -> {
                        serviceContainer
                                .getService(StorageService.class)
                                .cleanupTmp();
                    }, 3 * 60 * 60);

            if (serviceContainer.getService(ParametersService.class)
                    .getProperty("INTERNET_SERVER_CONNECTION")
                    .equals("1")) {
                serviceContainer.getService(SchedulerService.class)
                        .schedule(() -> {
                            serviceContainer
                                    .getService(CommunicatorService.class)
                                    .executeConnection();
                        }, 10);
            }

            /**
             * Maybe it's not good idea to set listener here. This listener will
             * update app`s state on channels` entities change.
             */
            serviceContainer.getService(EventService.class).getListeners().add(
                    new EntityEventListener(
                            (EntityEvent event) -> {
                                serviceContainer.getService(AppStateHolderService.class)
                                        .updateState();
                            },
                            ChannelEntity.class,
                            null,
                            null
                    )
            );
        } catch (Throwable e) {
            try {
                // It needs to try log error beause error could be in logging logic.
                LOG.log(Level.SEVERE, "Error on app initialization.", e);
            } finally {
                System.exit(1);
            }
        }
    }

    private void initConsoleLoggin() {
        Handler handler = new ConsoleHandler();
        Level level = Level.FINEST;
        handler.setLevel(level);

        ArrayList<String> classNames = new ArrayList<>(Arrays.asList(new String[]{ //            "",
        //            JavaFxPlatform.class.getName(),
        //            JavaFxSynchronous.class.getName(),
        //            TrackPlayer.class.getName(),
        //            JfxTrackPlayer.class.getName()
        }));

        Logger globalLogger = Logger.getLogger("");

//        Handler[] currentHandlers = globalLogger.getHandlers();
//        for (Handler currentHandler : currentHandlers) {
//            globalLogger.removeHandler(currentHandler);
//        }
        globalLogger.setLevel(level);
        globalLogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (!classNames.isEmpty() && classNames.contains(record.getLoggerName())
                        || classNames.isEmpty()) {
                    handler.publish(record);
                }
            }

            @Override
            public void flush() {
                handler.flush();
            }

            @Override
            public void close() throws SecurityException {
                handler.close();
            }
        });
    }

    private void initGui() {
        guiThread = new Thread(() -> Application.launch(ServerGui.class));
        guiThread.setName("GUI thread.");
        guiThread.start();
        try {
            ServerGui.waitForStar();
        } catch (InterruptedException | GuiInitTimeoutException ex) {
            LOG.log(Level.SEVERE, "Can't wait for GUI.", ex);
            System.exit(1);
        }
    }

    private void lockChecking() {
        try {
            AppLockManager.acquireAppLock(AppLockManager.AppLockMode.NAME);
        } catch (AlreadyLockedException ale) {
            LOG.log(Level.SEVERE, "Another instance already started.", ale);
            try {
                System.out.println(AppLockManager.sendMessage(ServerApp.name(), AppLockManager.Messages.GET_APP_INFO));
            } catch (UndeliveredMessageException ume) {
                LOG.log(Level.SEVERE, "Can't get info about lock.", ume);
            }
            System.exit(0);
        }
    }

    @Override
    protected void onExit() {
        if (Gui.instance().isShowing()) {
            Gui.instance().showHide();
        }
        serviceContainer.getService(WindowService.class).release();
        ((MainController) serviceContainer.getService(WindowService.class)
                .getWindow(MainWindow.class)
                .getController()).release();
        serviceContainer.getService(SchedulerService.class).release();
        Platform.exit();
        try {
            guiThread.join();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Can't wait for GUI thread.", ex);
        }
        serviceContainer.getService(HibernateService.class).release();
    }

    @Override
    public String getName() {
        return "VideoWeber Server";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getVersionInfo() {
        return String.format("%s %s", getName(), getVersion());
    }

}
