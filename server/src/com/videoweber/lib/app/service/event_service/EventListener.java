package com.videoweber.lib.app.service.event_service;

import java.util.function.Consumer;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EventListener {

    private final Consumer<Event> handler;

    public EventListener(Consumer<Event> handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        this.handler = handler;
    }

    public void onEvent(Event event) {
        if (event == null) {
            throw new NullPointerException();
        }
        handler.accept(event);
    }
}
