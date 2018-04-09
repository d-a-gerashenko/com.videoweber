package com.videoweber.server.service.track_service;

import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.track.Track;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.service.storage_service.StorageService;
import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelTrack implements Track {

    private final ChannelEntity channelEntity;
    private final TrackService trackService;

    public ChannelTrack(ChannelEntity channelEntity, TrackService trackService) {
        if (channelEntity == null
                || trackService == null) {
            throw new NullPointerException();
        }
        this.channelEntity = channelEntity;
        this.trackService = trackService;
    }

    @Override
    public Sample getSample(Date position) {
        SampleEntity sampleEntity = trackService.getSample(channelEntity, position);
        if (sampleEntity == null) {
            return null;
        }
        return trackService.getServiceContainer().getService(StorageService.class)
                .createSample(sampleEntity);
    }

    @Override
    public Sample getLastSample() {
        SampleEntity sampleEntity = trackService.getLastSample(channelEntity);
        if (sampleEntity == null) {
            return null;
        }
        return trackService.getServiceContainer().getService(StorageService.class)
                .createSample(sampleEntity);
    }

}
