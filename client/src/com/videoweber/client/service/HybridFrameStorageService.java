package com.videoweber.client.service;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.FrameEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.repository.FrameRepository;
import com.videoweber.client.repository.SampleRepository;
import com.videoweber.client.service.communicator_service.CommunicatorService;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetFrameRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetLastFrameRequestFactory;
import com.videoweber.internet.client.channel_request.ChFuture;
import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.internet.client.channel_request.FileEncoder;
import com.videoweber.lib.app.service.ParametersService;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.TempDirService;
import com.videoweber.lib.common.RandomStringGenerator;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class HybridFrameStorageService extends Service {

    private static final Logger LOG = Logger.getLogger(HybridFrameStorageService.class.getName());

    private final File tempDir;
    private final FileEncoder fileEncoder;

    public HybridFrameStorageService(ServiceContainer serviceContainer) {
        super(serviceContainer);
        tempDir = getServiceContainer().getService(TempDirService.class)
                .createTempDir();
        fileEncoder = new FileEncoder(
                getServiceContainer().getService(ParametersService.class)
                .getProperty("ENCRYPTION_KEY")
        );
    }

    public FrameEntity getFrame(ChannelEntity channelEntity, Date position) {
        if (channelEntity == null || position == null) {
            throw new NullPointerException();
        }
        
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        
        SampleEntity sampleEntity = sampleRepository.get(channelEntity, position);
        if (sampleEntity == null) {
            return null;
        }

        FrameRepository frameRepository = (FrameRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(FrameEntity.class);

        FrameEntity localFrame = frameRepository
                .get(
                        sampleEntity.getChannel(),
                        new Date(sampleEntity.getBegin())
                );
        if (localFrame != null) {
            return localFrame;
        }

        ChRequest chRequest = getServiceContainer()
                .getService(CommunicatorService.class)
                .getChRequestFactory(GetFrameRequestFactory.class)
                .createRequest(sampleEntity);
        ChFuture chFuture = new ChFuture(chRequest);

        getServiceContainer().getService(CommunicatorService.class)
                .process(chFuture);
        try {
            ChResponse chResponse = chFuture.getChResponse();
            if (chResponse.getStatus() == ChResponse.Status.ERROR) {
                throw new RuntimeException();
            }

            File loadedFrameFile = new File(
                    tempDir.getAbsolutePath() + File.separator
                    + RandomStringGenerator.generate() + ".jpg"
            );
            fileEncoder.decode(chResponse.getData().toString(), loadedFrameFile);

            return getServiceContainer().getService(FrameStorageService.class)
                    .importFrame(
                            new Date(sampleEntity.getBegin()),
                            loadedFrameFile,
                            sampleEntity.getChannel()
                    );
        } catch (RuntimeException ex) {
            LOG.log(Level.FINE, "Exception during chRequest.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public FrameEntity getLastFrame(ChannelEntity channelEntity) {
        FrameRepository frameRepository = (FrameRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(FrameEntity.class);
        FrameEntity localLastFrame = frameRepository.findLastByChannel(channelEntity);

        ChRequest chRequest = getServiceContainer()
                .getService(CommunicatorService.class)
                .getChRequestFactory(GetLastFrameRequestFactory.class)
                .createRequest(
                        channelEntity,
                        (localLastFrame == null) ? null : new Date(localLastFrame.getDate())
                );
        ChFuture chFuture = new ChFuture(chRequest);

        getServiceContainer().getService(CommunicatorService.class)
                .process(chFuture);
        try {
            ChResponse chResponse = chFuture.getChResponse();
            if (chResponse.getStatus() != ChResponse.Status.SUCCESS) {
                throw new RuntimeException();
            }

            JSONObject jo = (JSONObject) chResponse.getData();

            Date loadedFrameDate = new Date(jo.getLong("date"));
            File loadedFrameFile = new File(
                    tempDir.getAbsolutePath() + File.separator
                    + RandomStringGenerator.generate() + ".jpg"
            );
            fileEncoder.decode(jo.getString("file"), loadedFrameFile);

            return getServiceContainer().getService(FrameStorageService.class)
                    .importFrame(
                            loadedFrameDate,
                            loadedFrameFile,
                            channelEntity
                    );
        } catch (RuntimeException ex) {
            LOG.log(Level.FINE, "Exception during chRequest.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return localLastFrame;
    }

}
