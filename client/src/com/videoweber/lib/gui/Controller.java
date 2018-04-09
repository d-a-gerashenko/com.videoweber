package com.videoweber.lib.gui;

import com.videoweber.lib.app.App;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.ServiceContainerHolder;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class Controller implements
        Initializable, ServiceContainerHolder {

    private ServiceContainer SERVICE_CONTAINER;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public final void setServiceContainer(ServiceContainer serviceContainer) {
        if (serviceContainer == null) {
            throw new NullPointerException();
        }
        if (SERVICE_CONTAINER != null) {
            throw new RuntimeException("ServiceContainer is initialized.");
        }
        SERVICE_CONTAINER = serviceContainer;
    }

    @Override
    public final ServiceContainer getServiceContainer() {
        if (SERVICE_CONTAINER == null) {
            throw new NullPointerException("ServiceContainer not initialized.");
        }
        return SERVICE_CONTAINER;
    }

    public App getApp() {
        return App.instance();
    }

}
