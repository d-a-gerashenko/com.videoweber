package com.videoweber.lib.app.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ServiceContainer {

    private final HashMap<Class, Service> services = new HashMap<>();

    public synchronized <T extends Service> T getService(Class<T> serviceClass) {
        if (!services.containsKey(serviceClass)) {
            try {
                services.put(
                        serviceClass,
                        serviceClass.getConstructor(ServiceContainer.class).newInstance(this)
                );
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("Can't create service instance.", ex);
            }
        }
        return (T) services.get(serviceClass);
    }
}
