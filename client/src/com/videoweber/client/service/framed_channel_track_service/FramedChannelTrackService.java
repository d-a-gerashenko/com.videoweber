package com.videoweber.client.service.framed_channel_track_service;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FramedChannelTrackService extends Service {

    private final HashMap<UUID, FramedChannelTrack> framedChannelTracks = new HashMap<>();

    public FramedChannelTrackService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public synchronized FramedChannelTrack getFramedChannelTrack(ChannelEntity channelEntity) {
        if (!framedChannelTracks.containsKey(channelEntity.getUuid())) {
            framedChannelTracks.put(channelEntity.getUuid(), new FramedChannelTrack(channelEntity, this));
        }
        return framedChannelTracks.get(channelEntity.getUuid());
    }

}
