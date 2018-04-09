package com.videoweber.client.service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.common.VarManager;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.repository.SampleRepository;
import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.common.tmp_file_marker.TmpMarker;
import com.videoweber.lib.common.tmp_file_marker.TmpMarkers;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SampleStorageService extends Service {

    private static final Logger LOG = Logger.getLogger(SampleStorageService.class.getName());
    private final File storageDir;

    public SampleStorageService(ServiceContainer serviceContainer) {
        super(serviceContainer);
        storageDir = new File(VarManager.getRootDir() + File.separator + "samples");
        storageDir.mkdirs();
        if (!storageDir.exists()) {
            throw new RuntimeException("Can't create storage dir.");
        }
        if (!storageDir.canWrite()) {
            throw new RuntimeException("Storage dir isn't writable.");
        }
    }

    public SampleEntity importSampleDescriptor(
            UUID uuid,
            String samplerInfo,
            Date begin,
            String extension,
            int duration,
            int size,
            MediaType mediaType,
            boolean isRecorded,
            ChannelEntity channelEntity
    ) {
        if (uuid == null
                || samplerInfo == null
                || begin == null
                || extension == null
                || duration <= 0
                || size <= 0
                || mediaType == null
                || channelEntity == null) {
            throw new IllegalArgumentException();
        }
        if (samplerInfo.isEmpty()) {
            throw new IllegalArgumentException("Sampler info is empty.");
        }

        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        SampleEntity localSample = sampleRepository.get(uuid);
        if (localSample != null) {
            LOG.log(Level.WARNING, String.format("Sample \"%s\" already imported.", localSample.getUuid()));
            return localSample;
        }

        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setRemoteUuid(uuid);
        sampleEntity.setBegin(begin.getTime());
        sampleEntity.setEnd(begin.getTime() + duration);
        sampleEntity.setChannel(channelEntity);
        sampleEntity.setExtension(extension);
        sampleEntity.setMediaType(mediaType);
        sampleEntity.setRecorded(isRecorded);
        sampleEntity.setSamplerInfo(samplerInfo);
        sampleEntity.setSize(size);

        getServiceContainer().getService(HibernateService.class).save(sampleEntity);

        return sampleEntity;
    }

    public File importSampleFile(SampleEntity sampleEntity, File sampleFile) {
        if (sampleEntity == null || sampleFile == null) {
            throw new NullPointerException();
        }
        if (sampleEntity.isLoaded()) {
            throw new IllegalArgumentException("Sample file already loaded.");
        }
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            Transaction tx = session.beginTransaction();
            try {
                sampleEntity.setLoaded(true);
                session.update(sampleEntity);
                session.flush();

                TmpMarker tmpMarker = new TmpMarker(getFile(sampleEntity));
                tmpMarker.createFlag();
                try {
                    Files.move(sampleFile.toPath(), tmpMarker.getFile().toPath());
                } catch (IOException ex) {
                    throw new RuntimeException(
                            String.format(
                                    "Can't import sample file \"%s\" of channel \"%s\" (#%s) to \"%s\"",
                                    sampleFile.getAbsolutePath(),
                                    sampleEntity.getChannel().getTitle(),
                                    sampleEntity.getChannel().getUuid(),
                                    getFile(sampleEntity)
                            ),
                            ex
                    );
                }

                tx.commit();
                tmpMarker.deleteFlag();
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(
                        String.format("Can't import sample file (%s) for sample (%s).", sampleFile.getAbsolutePath(), sampleEntity.getUuid()),
                        e
                );
            }
        });
        return getFile(sampleEntity);
    }

    public File getFile(SampleEntity sampleEntity) {
        return new File(
                storageDir + File.separator + sampleEntity.getUuid() + "." + sampleEntity.getExtension()
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
                session.delete(sampleEntity);
                session.flush();

                TmpMarker tmpMarker = new TmpMarker(getFile(sampleEntity));
                tmpMarker.createFlag();
                tx.commit();
                if (tmpMarker.getFile().exists()) {
                    try {
                        Files.delete(getFile(sampleEntity).toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(String.format("Cant't delete sample file.", getFile(sampleEntity).getAbsolutePath()), e);
                    }
                }
                tmpMarker.deleteFlag();
            } catch (HibernateException e) {
                if (tx != null) {
                    tx.rollback();
                }
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

    public void cleanupTmp() {
        TmpMarkers.cleanupDirectory(storageDir, (File file) -> {
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
    }
}
