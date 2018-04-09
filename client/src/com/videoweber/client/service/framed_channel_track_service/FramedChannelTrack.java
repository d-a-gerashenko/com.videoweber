package com.videoweber.client.service.framed_channel_track_service;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.FrameEntity;
import com.videoweber.client.repository.FrameRepository;
import com.videoweber.client.service.FrameStorageService;
import com.videoweber.client.service.HibernateService;
import com.videoweber.client.service.HybridFrameStorageService;
import com.videoweber.lib.framer.Frame;
import com.videoweber.lib.framer.FramedTrackInterface;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.FutureTask;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FramedChannelTrack implements FramedTrackInterface {

    private final ChannelEntity channelEntity;
    private final FramedChannelTrackService trackService;
    private HashMap<Long, FutureTask<FrameEntity>> loadFrameTasks = new HashMap<>();
    private FutureTask<FrameEntity> loadLastFrameTask = null;
    private Date lastFrameLastRequest;

    public FramedChannelTrack(ChannelEntity channelEntity, FramedChannelTrackService trackService) {
        if (channelEntity == null
                || trackService == null) {
            throw new NullPointerException();
        }
        this.channelEntity = channelEntity;
        this.trackService = trackService;
        lastFrameLastRequest = new Date(0);
    }

    @Override
    public synchronized Frame getFrame(Date position) {
        if (position == null) {
            throw new NullPointerException();
        }
        Long time = position.getTime();

        FrameRepository frameRepository = (FrameRepository) trackService
                .getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(FrameEntity.class);

        FrameEntity frameEntity = frameRepository.get(channelEntity, position);

        if (frameEntity == null
                && !loadFrameTasks.containsKey(time)) {
            FutureTask<FrameEntity> newLoadFrameTask = new FutureTask<>(() -> {
                try {
                    FrameEntity loadedFrame = trackService
                            .getServiceContainer()
                            .getService(HybridFrameStorageService.class)
                            .getFrame(channelEntity, position);
                } finally {
                    synchronized (this) {
                        loadFrameTasks.remove(time);
                    }
                }
            }, null);
            new Thread(newLoadFrameTask).start();
        }

        return frameEntityToFrame(frameEntity);
    }

    @Override
    public synchronized Frame getLastFrame() {
        FrameRepository frameRepository = (FrameRepository) trackService
                .getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(FrameEntity.class);

        FrameEntity frameEntity = frameRepository.findLastByChannel(channelEntity);

        if (loadLastFrameTask == null
                && System.currentTimeMillis() - lastFrameLastRequest.getTime() > 600000) {
            lastFrameLastRequest = new Date();
            loadLastFrameTask = new FutureTask<>(() -> {
                try {
                    trackService
                            .getServiceContainer()
                            .getService(HybridFrameStorageService.class)
                            .getLastFrame(channelEntity);
                } finally {
                    synchronized (this) {
                        loadLastFrameTask = null;
                    }
                }
            }, null);
            new Thread(loadLastFrameTask).start();
        }

        return frameEntityToFrame(frameEntity);
    }

    private Frame frameEntityToFrame(FrameEntity frameEntity) {
        FrameStorageService frameStorageService = trackService
                .getServiceContainer()
                .getService(FrameStorageService.class);
        return (frameEntity == null) ? null : frameStorageService.createFrame(frameEntity);
    }

}
