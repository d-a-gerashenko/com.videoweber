package com.videoweber.lib.app.service;

import java.util.Objects;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class Service {

    private final ServiceContainer serviceContainer;

    public Service(ServiceContainer serviceContainer) {
        Objects.requireNonNull(serviceContainer);
        this.serviceContainer = serviceContainer;
    }

    public final ServiceContainer getServiceContainer() {
        return serviceContainer;
    }

}
