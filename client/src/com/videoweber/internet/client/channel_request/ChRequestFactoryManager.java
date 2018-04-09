package com.videoweber.internet.client.channel_request;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChRequestFactoryManager {

    private final Map<Class<? extends ChRequestFactory>, ChRequestFactory> factories = new HashMap<>();

    public void registerFactory(ChRequestFactory factory) {
        if (factories.containsKey(factory.getClass())) {
            throw new RuntimeException(
                    String.format(
                            "Factory \"%s\" already registered.",
                            factory.getClass().getName()
                    )
            );
        }
        factories.put(factory.getClass(), factory);
    }

    public <T extends ChRequestFactory> T getFactory(Class<T> factoryClass) {
        ChRequestFactory factory = factories.get(factoryClass);
        if (factory == null) {
            throw new RuntimeException(
                    String.format(
                            "Factory \"%s\" isn't registered.",
                            factoryClass.getClass().getName()
                    )
            );
        }

        return (T) factory;
    }
}
