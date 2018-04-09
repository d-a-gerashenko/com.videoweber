package com.videoweber.lib.app.service;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface ServiceContainerHolder {

    public void setServiceContainer(ServiceContainer serviceContainer);

    public ServiceContainer getServiceContainer();

    public default <T extends Service> T getService(Class<T> serviceClass) {
        return (T)getServiceContainer().getService(serviceClass);
    }
}
