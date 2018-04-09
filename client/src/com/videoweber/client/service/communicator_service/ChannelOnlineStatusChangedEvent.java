package com.videoweber.client.service.communicator_service;

import com.videoweber.lib.app.service.event_service.Event;
import java.util.UUID;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelOnlineStatusChangedEvent extends Event {

    public static enum Status {
        ONLINE, OFFLINE;
    };

    private final Status status;
    private final UUID channel;

    public ChannelOnlineStatusChangedEvent(Status status, UUID channel) {
        if (status == null
                ||channel == null) {
            throw new NullPointerException();
        }
        this.status = status;
        this.channel = channel;
    }

    public Status getStatus() {
        return status;
    }

    public UUID getChannel() {
        return channel;
    }
}
