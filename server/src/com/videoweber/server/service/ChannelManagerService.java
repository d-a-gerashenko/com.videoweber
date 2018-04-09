package com.videoweber.server.service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.channel.Channel;
import com.videoweber.lib.channel.Source;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.EffectEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.entity.SourceEntity;
import com.videoweber.server.entity.TriggerEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.repository.EffectRepository;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.repository.TriggerRepository;
import java.util.NoSuchElementException;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelManagerService extends Service {

    public ChannelManagerService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public Channel createChannel(ChannelEntity channelEntity) {
        SourceEntity videoSourceEntity = channelEntity.getVideoSource();
        SourceEntity audioSourceEntity = channelEntity.getAudioSource();

        SourceManagerService sourceManagerService = getServiceContainer()
                .getService(SourceManagerService.class);

        Source videoSource = (videoSourceEntity == null)
                ? null : sourceManagerService.createSource(videoSourceEntity);

        Source audioSource = (audioSourceEntity == null)
                ? null : sourceManagerService.createSource(audioSourceEntity);

        return new Channel(videoSource, audioSource);
    }

    public void delete(ChannelEntity channelEntity) {
        HibernateService hibernateService = getServiceContainer()
                .getService(HibernateService.class);

        ((SampleRepository) hibernateService
                .getRepository(SampleEntity.class))
                .deleteByChannel(channelEntity);

        ((EffectRepository) hibernateService
                .getRepository(EffectEntity.class))
                .deleteByChannel(channelEntity);

        ((TriggerRepository) hibernateService
                .getRepository(TriggerEntity.class))
                .deleteByChannel(channelEntity);

        getServiceContainer().getService(HibernateService.class).delete(channelEntity);
    }

    /**
     * @param period In seconds.
     * @return
     */
    public long getSizePer(long period) {
        ChannelRepository channelRepository = (ChannelRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(ChannelEntity.class);
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        long sizeOfPeriod = 0;
        try {
            sizeOfPeriod = channelRepository.findAll().stream()
                    .map((channelEntity) -> sampleRepository.findLastByChannel(channelEntity))
                    .map((sampleEntity) -> {
                        long channelSizeOfPeriod = 0;

                        if (sampleEntity != null) {
                            channelSizeOfPeriod = sampleEntity.getSize() * period * 1000 / sampleEntity.getDuration();
                        }
                        return channelSizeOfPeriod;
                    })
                    .reduce(sizeOfPeriod, (accumulator, channelSizeOfPeriod) -> {
                        return accumulator + channelSizeOfPeriod;
                    });
        } catch (NoSuchElementException nsee) {
            // Ignoring
        }
        return sizeOfPeriod;
    }

}
