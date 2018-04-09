package com.videoweber.lib.app.service.widget_service;

import javafx.scene.Parent;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Widget {

    private final Parent rootNode;
    private final WidgetController controller;
    private boolean initialized = false;

    public Widget(Parent rootNode, WidgetController controller) {
        this.rootNode = rootNode;
        this.controller = controller;
    }

    public Parent getRootNode() {
        return rootNode;
    }

    public WidgetController getController() {
        return controller;
    }

    public void setInitialized() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
