package com.videoweber.client.service.channel_track_service;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.repository.SampleRepository;
import com.videoweber.client.service.HibernateService;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelTrackService extends Service {

    public ChannelTrackService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public ChannelTrack createChannelTrack(ChannelEntity channelEntity) {
        return new ChannelTrack(channelEntity, this);
    }

    public SampleEntity getSample(ChannelEntity channelEntity, Date position) {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        SampleEntity sample = sampleRepository.findNextLoadedSample(channelEntity, position);
        return sample;
    }

    public SampleEntity getLastSample(ChannelEntity channelEntity) {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        return sampleRepository.findLastLoadedByChannel(channelEntity);
    }

}
