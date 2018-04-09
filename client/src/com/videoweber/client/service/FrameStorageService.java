package com.videoweber.client.service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.common.VarManager;
import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.FrameEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.repository.FrameRepository;
import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.common.tmp_file_marker.TmpMarker;
import com.videoweber.lib.common.tmp_file_marker.TmpMarkers;
import com.videoweber.lib.framer.Frame;
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
public class FrameStorageService extends Service {

    private static final Logger LOG = Logger.getLogger(FrameStorageService.class.getName());
    private final File storageDir;

    public FrameStorageService(ServiceContainer serviceContainer) {
        super(serviceContainer);
        storageDir = new File(VarManager.getRootDir() + File.separator + "frames");
        storageDir.mkdirs();
        if (!storageDir.exists()) {
            throw new RuntimeException("Can't create storage dir.");
        }
        if (!storageDir.canWrite()) {
            throw new RuntimeException("Storage dir isn't writable.");
        }
    }

    public FrameEntity importFrame(Date date, File file, ChannelEntity channelEntity) {
        if (date == null
                || file == null) {
            throw new IllegalArgumentException();
        }

        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("Frame file doesn't exist: %s", file.getAbsolutePath()));
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(String.format("Frame file isn't readable: %s", file.getAbsolutePath()));
        }

        FrameRepository frameRepository = (FrameRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(FrameEntity.class);
        FrameEntity localFrame = frameRepository.get(channelEntity, date);
        if (localFrame != null) {
            LOG.log(Level.WARNING, String.format("Frame \"%s\" already imported.", localFrame.getUuid()));
            return localFrame;
        }

        FrameEntity frameEntity = new FrameEntity();
        frameEntity.setChannel(channelEntity);
        frameEntity.setDate(date.getTime());
        frameEntity.setSize((int) file.length());
        frameEntity.setExtension(FileNameFunstions.extension(file.getName()));

        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(frameEntity);
                session.flush();

                TmpMarker tmpMarker = new TmpMarker(getFile(frameEntity));
                tmpMarker.createFlag();
                try {
                    Files.move(file.toPath(), tmpMarker.getFile().toPath());
                } catch (IOException ex) {
                    throw new RuntimeException(
                            String.format(
                                    "Can't import frame file \"%s\" (%s) of channel \"%s\" to \"%s\"",
                                    file.getAbsolutePath(),
                                    frameEntity.getChannel().getTitle(),
                                    frameEntity.getChannel().getUuid(),
                                    getFile(frameEntity)
                            ),
                            ex
                    );
                }

                tx.commit();
                tmpMarker.deleteFlag();
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(
                        String.format("Can't import frame file (%s) for sample (%s).", file.getAbsolutePath(), frameEntity.getUuid()),
                        e
                );
            }
        });

        return frameEntity;
    }

    public File getFile(FrameEntity frameEntity) {
        return new File(
                storageDir + File.separator + frameEntity.getUuid() + "." + frameEntity.getExtension()
        );
    }

    public Frame createFrame(FrameEntity frameEntity) {
        return new Frame(new Date(frameEntity.getDate()), getFile(frameEntity));
    }

    public void delete(FrameEntity frameEntity) {
        if (frameEntity == null) {
            throw new NullPointerException();
        }
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            Transaction tx = session.beginTransaction();
            try {
                session.delete(frameEntity);
                session.flush();

                TmpMarker tmpMarker = new TmpMarker(getFile(frameEntity));
                tmpMarker.createFlag();
                tx.commit();
                try {
                    Files.delete(tmpMarker.getFile().toPath());
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Cant't delete FrameEntity file.", getFile(frameEntity).getAbsolutePath()), e);
                }

                tmpMarker.deleteFlag();
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(
                        String.format("Can't delete FrameEntity with file %s.", getFile(frameEntity).getAbsolutePath()),
                        e
                );
            }
        });
    }

    public void cleanupDeleted() {
        FrameRepository frameRepository = (FrameRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(FrameEntity.class);
        List<FrameEntity> toDelete;
        do {
            toDelete = frameRepository.findOldDeleted();
            toDelete.forEach((frameEntity) -> {
                delete(frameEntity);
            });
        } while (!toDelete.isEmpty());
    }

    public void cleanupTmp() {
        TmpMarkers.cleanupDirectory(storageDir, (File file) -> {
            return null == getServiceContainer()
                    .getService(HibernateService.class)
                    .getRepository(FrameEntity.class)
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
