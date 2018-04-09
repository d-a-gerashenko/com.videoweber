package com.videoweber.lib.app.service.event_service;

import java.util.function.Consumer;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 * @param <T>
 */
public class SpecifiedEventListener<T extends Event> extends EventListener {

    private final Class<T> eventClass;

    public SpecifiedEventListener(Class<T> eventClass, Consumer<T> handler) {
        super((Event event) -> {
            if (!eventClass.isAssignableFrom(event.getClass())) {
                return;
            }
            handler.accept((T) event);
        });
        if (eventClass == null
                || handler == null) {
            throw new NullPointerException();
        }
        this.eventClass = eventClass;
    }

    public Class<T> getEventClass() {
        return eventClass;
    }

}
