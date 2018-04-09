package com.videoweber.client.service.channel_track_service;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.service.SampleStorageService;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.track.Track;
import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelTrack implements Track {

    private final ChannelEntity channelEntity;
    private final ChannelTrackService trackService;

    public ChannelTrack(ChannelEntity channelEntity, ChannelTrackService trackService) {
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
        return trackService.getServiceContainer().getService(SampleStorageService.class)
                .createSample(sampleEntity);
    }

    @Override
    public Sample getLastSample() {
        SampleEntity sampleEntity = trackService.getLastSample(channelEntity);
        if (sampleEntity == null) {
            return null;
        }
        return trackService.getServiceContainer().getService(SampleStorageService.class)
                .createSample(sampleEntity);
    }

}
