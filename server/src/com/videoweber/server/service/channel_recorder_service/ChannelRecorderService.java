package com.videoweber.server.service.channel_recorder_service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.TempDirService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.channel.Channel;
import com.videoweber.lib.engines.javacv.FfmpegEffectProcessor;
import com.videoweber.lib.engines.javacv.FfmpegProbeFactory;
import com.videoweber.lib.engines.javacv.FfmpegSamplerEngine;
import com.videoweber.lib.recorder.Recorder;
import com.videoweber.lib.sampler.EffectProcessor;
import com.videoweber.lib.sampler.ProbeFactory;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.sampler.SampleFactory;
import com.videoweber.lib.sampler.Sampler;
import com.videoweber.lib.sampler.SamplerEngine;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.EffectEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.entity.SourceEntity;
import com.videoweber.server.entity.TriggerEntity;
import com.videoweber.server.repository.EffectRepository;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.repository.TriggerRepository;
import com.videoweber.server.service.ChannelManagerService;
import com.videoweber.server.service.EffectManagerService;
import com.videoweber.server.service.HibernateService;
import com.videoweber.server.service.TriggerManagerService;
import com.videoweber.server.service.storage_service.NoSpaceException;
import com.videoweber.server.service.storage_service.StorageService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelRecorderService extends Service {

    private static final Logger LOG = Logger.getLogger(ChannelRecorderService.class.getName());

    private final Map<UUID, ChannelRecorder> resorders = new HashMap<>();
    private final Map<UUID, Set<EntityEventListener>> listeners = new HashMap<>();

    public ChannelRecorderService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public synchronized ChannelRecorder getChannelRecorder(ChannelEntity channelEntity) {
        Objects.requireNonNull(channelEntity);
        Objects.requireNonNull(channelEntity.getUuid());
        if (!resorders.containsKey(channelEntity.getUuid())) {
            resorders.put(
                    channelEntity.getUuid(),
                    new ChannelRecorder(() -> {
                        return createSampler(channelEntity);
                    })
            );
        }
        return resorders.get(channelEntity.getUuid());
    }

    private Sampler createSampler(ChannelEntity channelEntity) {
        unregisterAllListeners(channelEntity);
        try {
            registerListener(channelEntity, new EntityEventListener(
                    (EntityEvent event) -> {
                        getServiceContainer().getService(HibernateService.class)
                                .refresh(channelEntity);
                        getChannelRecorder(channelEntity).refresh();
                    },
                    ChannelEntity.class, null, channelEntity.getUuid()
            ));

            Channel channel = getServiceContainer()
                    .getService(ChannelManagerService.class)
                    .createChannel(channelEntity);

            if (channelEntity.getVideoSource() != null) {
                registerListener(channelEntity, new EntityEventListener(
                        (EntityEvent event) -> {
                            getChannelRecorder(channelEntity).refresh();
                        },
                        SourceEntity.class, null, channelEntity.getVideoSource().getId()
                ));
            }
            if (channelEntity.getAudioSource() != null) {
                registerListener(channelEntity, new EntityEventListener(
                        (EntityEvent event) -> {
                            getChannelRecorder(channelEntity).refresh();
                        },
                        SourceEntity.class, null, channelEntity.getAudioSource().getId()
                ));
            }

            // SAMPLER
            SamplerEngine samplerEngine = new FfmpegSamplerEngine(
                    channel,
                    getServiceContainer()
                            .getService(TempDirService.class).createTempDir()
            );
            ProbeFactory probeFactory = new FfmpegProbeFactory();
            SampleFactory sampleFactory = new SampleFactory(probeFactory);
            Sampler sampler = new Sampler(samplerEngine, sampleFactory);

            // EFFECT PROCESSOR
            EffectProcessor effectProcessor = new FfmpegEffectProcessor(
                    getServiceContainer().getService(TempDirService.class).createTempDir()
            );
            EffectRepository effectRepository = (EffectRepository) getServiceContainer()
                    .getService(HibernateService.class)
                    .getRepository(EffectEntity.class);
            List<EffectEntity> effects = effectRepository.findAllByChannel(channelEntity);
            effects.forEach((EffectEntity effectEntity) -> {
                effectProcessor.getEffects().add(
                        getServiceContainer().getService(EffectManagerService.class).createEffect(effectEntity)
                );
            });
            sampler.setSampleHandler(effectProcessor);
            effects.forEach((EffectEntity effectEntity) -> {
                registerListener(channelEntity, new EntityEventListener(
                        (EntityEvent event) -> {
                            getChannelRecorder(channelEntity).refresh();
                        },
                        EffectEntity.class, null, effectEntity.getId()
                ));
            });

            // RECORDER
            Recorder recorder = new Recorder();
            TriggerRepository triggerRepository = (TriggerRepository) getServiceContainer()
                    .getService(HibernateService.class)
                    .getRepository(TriggerEntity.class);
            List<TriggerEntity> triggers = triggerRepository.findAllByChannel(channelEntity);
            triggers.forEach((TriggerEntity triggerEntity) -> {
                recorder.getTriggers().add(
                        getServiceContainer().getService(TriggerManagerService.class).createTrigger(triggerEntity)
                );
            });
            effectProcessor.setSampleHandler((Sample sample) -> {
                SampleEntity sampleEntity;
                try {
                    sampleEntity = getServiceContainer().getService(StorageService.class).importSample(sample, channelEntity);
                    getServiceContainer().getService(EventService.class)
                            .trigger(
                                    new EntityEvent(
                                            SampleEntity.class,
                                            EntityOperation.CREATE,
                                            sampleEntity.getUuid()
                                    )
                            );
                } catch (NoSpaceException ex) {
                    throw new RuntimeException(ex);
                }
                Sample sampleInStorage = getServiceContainer().getService(StorageService.class).createSample(sampleEntity);
                recorder.onSample(sampleInStorage);
            });
            SampleRepository sampleRepository = (SampleRepository) getServiceContainer().getService(HibernateService.class)
                    .getRepository(SampleEntity.class);
            recorder.setRecordHandler((Sample sample) -> {
                SampleEntity sampleEntity = sampleRepository.get(channelEntity, sample.getBegin());
                if (sampleEntity == null) {
                    throw new NullPointerException(
                            String.format(
                                    "Can't find entity for sample \"%s\".",
                                    sampleEntity
                            )
                    );
                }
                sampleEntity.setRecorded(true);
                getServiceContainer().getService(HibernateService.class).update(sampleEntity);
            });
            triggers.forEach((TriggerEntity triggerEntity) -> {
                registerListener(channelEntity, new EntityEventListener(
                        (EntityEvent event) -> {
                            getChannelRecorder(channelEntity).refresh();
                        },
                        TriggerEntity.class, null, triggerEntity.getId()
                ));
            });

            return sampler;
        } catch (Exception e) {
            return null;
        }
    }

    public void release() {
        resorders.values().forEach((recorder) -> {
            recorder.stop();
        });
    }

    private void registerListener(ChannelEntity channelEntity, EntityEventListener entityEventListener) {
        if (!listeners.containsKey(channelEntity.getUuid())) {
            listeners.put(channelEntity.getUuid(), new HashSet<>());
        }
        listeners.get(channelEntity.getUuid()).add(entityEventListener);
        getServiceContainer().getService(EventService.class).getListeners().add(entityEventListener);
    }

    private void unregisterAllListeners(ChannelEntity channelEntity) {
        if (listeners.containsKey(channelEntity.getUuid())) {
            getServiceContainer().getService(EventService.class)
                    .getListeners()
                    .removeAll(listeners.get(channelEntity.getUuid()));
        }
        listeners.remove(channelEntity.getUuid());
    }

}
