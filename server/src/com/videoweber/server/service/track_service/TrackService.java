package com.videoweber.server.service.track_service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.service.HibernateService;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TrackService extends Service {

    public TrackService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public ChannelTrack createChannelTrack(ChannelEntity channelEntity) {
        return new ChannelTrack(channelEntity, this);
    }

    public SampleEntity getSample(ChannelEntity channelEntity, Date position) {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        List<SampleEntity> samples = sampleRepository.findNextSamples(channelEntity, position, 1);
        if (samples.isEmpty()) {
            return null;
        }
        return samples.get(0);
    }

    public SampleEntity getLastSample(ChannelEntity channelEntity) {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        return sampleRepository.findLastByChannel(channelEntity);
    }

}
