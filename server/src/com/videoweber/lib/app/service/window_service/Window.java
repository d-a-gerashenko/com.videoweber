package com.videoweber.lib.app.service.window_service;

import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class Window {

    private final Stage stage;
    private final WindowController controller;
    private boolean initialized = false;

    public Window(Stage stage, WindowController controller) {
        this.stage = stage;
        this.controller = controller;
    }

    public Stage getStage() {
        return stage;
    }

    public WindowController getController() {
        return controller;
    }

    public void setInitialized() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

}
