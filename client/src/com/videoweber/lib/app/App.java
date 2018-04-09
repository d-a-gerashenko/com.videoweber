package com.videoweber.lib.app;

import com.videoweber.lib.common.VarManager;
import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());

    private static App INSTANCE = null;
    private static final String SESSION_UID;
    private static final File PATH;
    private static final File WORKING_DIR;
    private static final CountDownLatch exitLatch = new CountDownLatch(1);

    static {
        SecureRandom RANDOM = new SecureRandom();
        SESSION_UID = new BigInteger(130, RANDOM).toString(32);

        PATH = new File(
                App.class.getProtectionDomain()
                        .getCodeSource().getLocation()
                        .getFile()
        );

        WORKING_DIR = new File(System.getProperty("user.dir"));
        if (!WORKING_DIR.exists()) {
            throw new RuntimeException(String.format("App working dir doesn't exist \"%s\".", WORKING_DIR));
        }
        if (!WORKING_DIR.canRead()) {
            throw new RuntimeException(String.format("App working dir doesn't accessible \"%s\".", WORKING_DIR));
        }
    }

    protected App() {
        initLoging();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!isExit()) {
                LOG.log(Level.SEVERE, "Incorrect app exit.");
            }
        }));
    }

    private void initLoging() {
        Logger globalLogger = Logger.getLogger("");
        Handler globalLogHandler = new MultiFileLogHandler(new File(VarManager.getRootDir() + File.separator + "log"));
        Level globalLogLevel = Level.FINE;
        globalLogHandler.setLevel(globalLogLevel);
        globalLogger.setLevel(globalLogLevel);
        globalLogger.addHandler(globalLogHandler);
    }

//    Example of initialization.
//    public static void init() {
//        NestedApp app = new NestedApp();
//        app.initInstance(app);
//    }
    public final static App instance() {
        if (INSTANCE == null) {
            throw new NullPointerException("App INSTANCE is not defined. Should call initInstance(app) in nested class.");
        }
        return INSTANCE;
    }

    protected final void initInstance(App app) {
        if (INSTANCE == null) {
            INSTANCE = app;
        } else {
            throw new RuntimeException("INSTANCE is already defined.");
        }
    }

    public final static String sessionUid() {
        return SESSION_UID;
    }

    public final String getSessionUid() {
        return SESSION_UID;
    }

    public final static File workingDir() {
        return WORKING_DIR;
    }

    public final File getWorkingDir() {
        return WORKING_DIR;
    }

    public final static File path() {
        return PATH;
    }

    public final File getPath() {
        return PATH;
    }

    public static String name() {
        return instance().getName();
    }

    public abstract String getName();

    public static String version() {
        return instance().getVersion();
    }

    public abstract String getVersion();

    public static String versionInfo() {
        return instance().getVersionInfo();
    }

    public abstract String getVersionInfo();

    public final synchronized static void exit() {
        if (isExit()) {
            return;
        }
        exitLatch.countDown();
        Thread exitThread = new Thread(() -> {
            try {
                instance().onExit();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error on exit.", e);
                System.exit(1);
            }
            Thread killerThread = new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    // Ignoring.
                }
                LOG.log(Level.SEVERE, "App wasn't closed on exit.");
                System.exit(1);
            });
            killerThread.setDaemon(true);
            killerThread.setName("Killer thread.");
            killerThread.start();
        });
        exitThread.setName("App exit thread.");
        exitThread.start();
    }

    public final static boolean isExit() {
        return exitLatch.getCount() == 0;
    }

    protected abstract void onExit();
}
