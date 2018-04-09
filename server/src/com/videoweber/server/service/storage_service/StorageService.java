package com.videoweber.server.service.storage_service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.common.tmp_file_marker.TmpMarker;
import com.videoweber.lib.common.tmp_file_marker.TmpMarkers;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.entity.StorageEntity;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.repository.StorageRepository;
import com.videoweber.server.service.ChannelManagerService;
import com.videoweber.server.service.HibernateService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class StorageService extends Service {

    private static final Logger LOG = Logger.getLogger(StorageService.class.getName());

    public StorageService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public StorageEntity getStorageToAllocate(long size) throws NoSpaceException {
        List<StorageEntity> storages = getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(StorageEntity.class)
                .findAll();
        if (storages.isEmpty()) {
            throw new RuntimeException();
        }
        for (StorageEntity storage : storages) {
            if (storage.getSizeFree() >= size) {
                return storage;
            }
        }
        throw new NoSpaceException();
    }

    public SampleEntity importSample(Sample sample, ChannelEntity channelEntity) throws NoSpaceException {
        if (sample == null
                || channelEntity == null) {
            throw new NullPointerException();
        }

        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setBegin(sample.getBegin().getTime());
        sampleEntity.setEnd(sample.getEnd().getTime());
        sampleEntity.setChannel(channelEntity);
        sampleEntity.setExtension(sample.getExtension());
        sampleEntity.setMediaType(sample.getMediaType());
        sampleEntity.setRecorded(false);
        sampleEntity.setSamplerInfo(sample.getSamplerInfo());
        sampleEntity.setSize(sample.getSize());

        StorageEntity storage = getStorageToAllocate(sample.getSize());

        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            Transaction tx = session.beginTransaction();
            try {
                sampleEntity.setStorage(storage);
                storage.setSizeUsed(storage.getSizeUsed() + sampleEntity.getSize());
                session.update(storage);

                session.persist(sampleEntity);
                session.flush();

                TmpMarker tmpMarker = new TmpMarker(getFile(sampleEntity));
                tmpMarker.createFlag();
                try {
                    Files.move(sample.getFile().toPath(), getFile(sampleEntity).toPath());
                } catch (IOException ex) {
                    throw new RuntimeException(
                            String.format(
                                    "Can't import sample \"%s\" of channel \"%s\" (#%s) to storage \"%s\"s (path: %s)",
                                    sample.getFile().getAbsolutePath(),
                                    channelEntity.getTitle(),
                                    channelEntity.getUuid(),
                                    storage.getId(),
                                    storage.getPath()
                            ),
                            ex
                    );
                }

                tx.commit();
                tmpMarker.deleteFlag();
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(
                        String.format("Can't add SampleEntity to database for sample (%s).", sample.getFile().getAbsolutePath()),
                        e
                );
            }
        });

        return sampleEntity;
    }

    public File getFile(SampleEntity sampleEntity) {
        return new File(
                sampleEntity.getStorage().getPath() + File.separator + sampleEntity.getUuid() + "." + sampleEntity.getExtension()
        );
    }

    public Sample createSample(SampleEntity sampleEntity) {
        return new Sample(
                sampleEntity.getSamplerInfo(),
                new Date(sampleEntity.getBegin()),
                sampleEntity.getExtension(),
                getFile(sampleEntity),
                Long.valueOf(sampleEntity.getEnd() - sampleEntity.getBegin()).intValue(),
                sampleEntity.getSize(),
                sampleEntity.getMediaType());
    }

    public void delete(SampleEntity sampleEntity) {
        if (sampleEntity == null) {
            throw new NullPointerException();
        }
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            Transaction tx = session.beginTransaction();
            try {
                StorageEntity storage = session.get(StorageEntity.class, sampleEntity.getStorage().getId());
                storage.setSizeUsed(storage.getSizeUsed() - sampleEntity.getSize());

                session.delete(sampleEntity);

                session.flush();

                TmpMarker tmpMarker = new TmpMarker(getFile(sampleEntity));
                tmpMarker.createFlag();

                tx.commit();

                try {
                    Files.delete(getFile(sampleEntity).toPath());
                } catch (IOException e) {
                    throw new RuntimeException("Cant't delete sample from storage.", e);
                }

                tmpMarker.deleteFlag();
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(
                        String.format("Can't delete SampleEntity with file %s.", getFile(sampleEntity).getAbsolutePath()),
                        e
                );
            }
        });
    }

    public void cleanupDeleted() {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        List<SampleEntity> toDelete;
        do {
            toDelete = sampleRepository.findOldDeleted();
            toDelete.forEach((sampleEntity) -> {
                delete(sampleEntity);
            });
        } while (!toDelete.isEmpty());
    }

    /**
     * @param period period In seconds.
     */
    public void allocateSpace(long period) {
        long sizeOfPeriod = getServiceContainer().getService(ChannelManagerService.class)
                .getSizePer(period);

        long availableSize = 0;

        availableSize += ((StorageRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(StorageEntity.class))
                .getCommonFreeSize();

        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        availableSize += sampleRepository
                .getCommonDeletedSize();

        while (availableSize < sizeOfPeriod) {
            SampleEntity firstRecordedSample = sampleRepository.getOldest();
            if (firstRecordedSample == null) {
                LOG.log(
                        Level.WARNING,
                        String.format(
                                "Can't allocate %sMB in %sMB storage.",
                                sizeOfPeriod / 1024 / 1024,
                                availableSize / 1024 / 1024
                        )
                );
                return;
            }
            sampleRepository.markDeleted(firstRecordedSample);
            availableSize += firstRecordedSample.getSize();
        }
    }

    public void cleanupTmp() {
        getServiceContainer().getService(HibernateService.class)
                .getRepository(StorageEntity.class)
                .findAll()
                .forEach((StorageEntity storageEntity) -> {
                    TmpMarkers.cleanupDirectory(new File(storageEntity.getPath()), (File file) -> {
                        return null == getServiceContainer()
                                .getService(HibernateService.class)
                                .getRepository(SampleEntity.class)
                                .get(
                                        UUID.fromString(
                                                FileNameFunstions.withoutExtension(
                                                        file.getName()
                                                )
                                        )
                                );
                    });
                });
    }
}
