package com.videoweber.lib.app.service.window_service;

import com.videoweber.lib.JavaFX.JavaFxSynchronous;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.gui.Gui;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class WindowService extends Service {

    private final Map<Class<? extends Window>, Window> windows = new HashMap<>();
    private final ArrayList<WeakReference<Window>> tempWindows = new ArrayList<>();

    public WindowService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public <T extends Window> T getWindow(Class<T> windowClass) {
        Objects.requireNonNull(windowClass);

        if (!windows.containsKey(windowClass)) {
            throw new RuntimeException(String.format("Window \"%s\" isn't initialized yet.", windowClass.getName()));
        }
        return (T) windows.get(windowClass);
    }

    private Window createWindow(Class<? extends Window> windowClass, Stage stage) {
        Objects.requireNonNull(windowClass);
        Objects.requireNonNull(stage);

        if (windows.containsKey(windowClass)) {
            throw new RuntimeException(String.format("Window \"%s\" is already initialized.", windowClass.getName()));
        }

        Scene scene;
        WindowController controller;
        try {
            FXMLLoader fXMLLoader = new FXMLLoader(
                    windowClass.getResource(getFxmlFileName(windowClass))
            );
            scene = new Scene(fXMLLoader.load());
            controller = fXMLLoader.getController();
            if (controller != null) {
                controller.setServiceContainer(getServiceContainer());
            }
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Can't load FXML for window \"%s\".", windowClass.getName()),
                    ex
            );
        }

        JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
            stage.setScene(scene);
        });

        Window window;
        try {
            window = windowClass.getConstructor(Stage.class, WindowController.class).newInstance(stage, controller);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(String.format("Can't crate window \"%s\".", windowClass.getName()), ex);
        }
        return window;
    }

    public void openWindow(Class<? extends Window> windowClass) {
        openWindow(windowClass, false, null);
    }

    public void openWindow(Class<? extends Window> windowClass, Object[] parameters) {
        openWindow(windowClass, false, parameters);
    }

    public void openWindow(Class<? extends Window> windowClass, boolean isTemp) {
        openWindow(windowClass, isTemp, null);
    }

    public synchronized void openWindow(Class<? extends Window> windowClass, boolean isTemp, Object[] parameters) {
        Objects.requireNonNull(windowClass);

        cleanupTempWindows();

        Window window;

        if (!isTemp) {
            if (!windows.containsKey(windowClass)) {
                Stage stage;
                if (PrimaryStageWindow.class.isAssignableFrom(windowClass)) {
                    for (Window w : windows.values()) {
                        if (w.getStage() == Gui.instance().getPrimaryStage()) {
                            throw new IllegalStateException("PrimaryStageWindow is alredy initialized.");
                        }
                    }
                    stage = Gui.instance().getPrimaryStage();
                } else {
                    stage = Gui.createStage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                }
                windows.put(windowClass, createWindow(windowClass, stage));
            }
            window = windows.get(windowClass);
        } else {
            if (PrimaryStageWindow.class.isAssignableFrom(windowClass)) {
                throw new IllegalStateException("PrimaryStageWindow couldn't be temp.");
            }
            Stage stage = Gui.createStage();
            stage.initModality(Modality.APPLICATION_MODAL);
            window = createWindow(windowClass, stage);
        }

        if (!Gui.instance().isShowing()) {
            Gui.instance().showHide();
        }

        /**
         * If a window already is showing there is no need to show it again
         * because if we will do this the window will cover current modal
         * window.
         */
        if (!window.getStage().isShowing()) {
            JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                EventHandler<WindowEvent> onStageShowing = window.getStage().getOnShowing();

                window.getStage().setOnShowing((WindowEvent onShowingEvent) -> {
                    window.getStage().setOnShowing(onStageShowing);
                    if (window.getStage().getOnShowing() != null) {
                        window.getStage().getOnShowing().handle(onShowingEvent);
                    }

                    // onStageShown could be changed in onStageShowing.
                    EventHandler<WindowEvent> onStageShown = window.getStage().getOnShown();

                    window.getStage().setOnShown((WindowEvent onShownEvent) -> {
                        window.getStage().setOnShown(onStageShown);
                        window.getStage().setAlwaysOnTop(true);
                        window.getStage().setAlwaysOnTop(false);
                        if (!window.isInitialized()) {
                            if (window.getController() != null) {
                                window.getController().onWindowInitialized();
                                window.getStage().sizeToScene();
                            }
                            window.setInitialized();
                        }
                        if (window.getController() != null) {
                            window.getController().onWindowOpened(parameters);
                            window.getStage().sizeToScene();
                        }
                        if (window.getStage().getOnShown() != null) {
                            window.getStage().getOnShown().handle(onShownEvent);
                        }
                    });
                });
                window.getStage().show();
            });
        }
    }

    private void cleanupTempWindows() {
        Iterator<WeakReference<Window>> it = tempWindows.iterator();
        while (it.hasNext()) {
            WeakReference<Window> windowReference = it.next();
            if (windowReference.get() == null) {
                tempWindows.remove(windowReference);
            }
        }
    }

    private static String getFxmlFileName(Class<? extends Window> windowClass) {
        String str = windowClass.getSimpleName();
        int ind = str.lastIndexOf("Window");
        if (ind > 0) {
            str = str.substring(0, ind);
        }
        return str + ".fxml";
    }

    public synchronized void release() {
        JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
            windows.values().forEach((window) -> {
                if (!(window instanceof PrimaryStageWindow)) {
                    requestWindowClose(window);
                }
            });
            tempWindows.forEach((windowRefenece) -> {
                Window tempWindow = windowRefenece.get();
                if (tempWindow != null) {
                    requestWindowClose(tempWindow);
                }
            });
        });
    }

    private void requestWindowClose(Window window) {
        EventHandler<WindowEvent> eventHandler = window.getStage()
                .getOnCloseRequest();
        if (eventHandler != null) {
            eventHandler.handle(
                    new WindowEvent(
                            window.getStage(),
                            WindowEvent.WINDOW_CLOSE_REQUEST
                    )
            );
        }
    }
}
