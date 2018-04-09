package com.videoweber.lib.app.service.event_service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EventService extends Service {

    private final List<EventListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public EventService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public List<EventListener> getListeners() {
        return listeners;
    }

    public <T extends Event> void trigger(T event) {
        if (event == null) {
            throw new NullPointerException();
        }

        /**
         * Listener shouldn't be execution during removing it from listeners.
         * This synchronization prevents listener execution after it was removed
         * from listeners.
         */
        synchronized (listeners) {
            /**
             * toArray(...) allows to remove a listener from it's body without
             * ConcurrentModificationException.
             */
            for (EventListener listener : listeners.toArray(new EventListener[listeners.size()])) {
                listener.onEvent(event);
            }
        }
    }
}
