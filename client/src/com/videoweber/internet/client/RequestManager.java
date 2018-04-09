package com.videoweber.internet.client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class RequestManager {

    private final Client client;
    private final Map<Class<? extends Request>, ResponseProcessor> processors = new HashMap<>();
    private final Map<Class<? extends RequestFactory>, RequestFactory> factories = new HashMap<>();

    public RequestManager(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void processRequest(Request request) {
        Response response = client.send(request);
        if (response.getStatus() == Response.Status.ERROR) {
            throw new RuntimeException("Server returned error description: " + (String) response.getData());
        }

        ResponseProcessor processor = processors.get(request.getClass());
        if (processor == null) {
            throw new RuntimeException(
                    String.format(
                            "Processor for \"%s\" isn't registered.",
                            request.getClass().getName()
                    )
            );
        }

        try {
            processor.process(request, response, this);
        } catch (Exception ex) {
            throw new RuntimeException("RequestProcessor can't process response: " + response.getData(), ex);
        }
    }

    public void registerProcessor(ResponseProcessor processor) {
        if (processors.containsKey(processor.getRequestClass())) {
            throw new RuntimeException(
                    String.format(
                            "Processor for \"%s\" already registered.",
                            processor.getRequestClass().getName()
                    )
            );
        }
        processors.put(processor.getRequestClass(), processor);
    }

    public void registerFactory(RequestFactory factory) {
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

    public <T extends RequestFactory> T getFactory(Class<T> factoryClass) {
        T factory = (T) factories.get(factoryClass);
        if (factory == null) {
            throw new RuntimeException(
                    String.format(
                            "Factory \"%s\" isn't registered.",
                            factoryClass.getName()
                    )
            );
        }
        return factory;
    }
}
