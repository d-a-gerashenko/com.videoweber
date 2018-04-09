package com.videoweber.client.service;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.repository.SampleRepository;
import com.videoweber.client.service.communicator_service.CommunicatorService;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.CheckUnrecordedSamplesRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetFirstSampleRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetSampleFileRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetSamplesRequestFactory;
import com.videoweber.internet.client.channel_request.ChFuture;
import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.internet.client.channel_request.FileEncoder;
import com.videoweber.lib.app.service.ParametersService;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.TempDirService;
import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.common.RandomStringGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class HybridSampleStorageService extends Service {

    private static final Logger LOG = Logger.getLogger(HybridSampleStorageService.class.getName());

    private final File tempDir;
    private final FileEncoder fileEncoder;

    public HybridSampleStorageService(ServiceContainer serviceContainer) {
        super(serviceContainer);
        tempDir = getServiceContainer().getService(TempDirService.class)
                .createTempDir();
        fileEncoder = new FileEncoder(
                getServiceContainer().getService(ParametersService.class)
                        .getProperty("ENCRYPTION_KEY")
        );
    }

    private SampleEntity importSampleJSONObject(
            JSONObject sampleJO,
            ChannelEntity channelEntity
    ) {
        return getServiceContainer()
                .getService(SampleStorageService.class)
                .importSampleDescriptor(
                        UUID.fromString(sampleJO.getString("uid")),
                        sampleJO.getString("samplerInfo"),
                        new Date(sampleJO.getLong("begin")),
                        sampleJO.getString("extension"),
                        sampleJO.getInt("duration"),
                        sampleJO.getInt("size"),
                        MediaType.valueOf(sampleJO.getString("mediaType")),
                        sampleJO.getBoolean("isRecorded"),
                        channelEntity
                );
    }

    public void loadNewSamples() {
        getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .findAll()
                .forEach((ChannelEntity channelEntity) -> {
                    while (getNewSamples(channelEntity).size() > 10) {

                    }
                });
    }

    public List<SampleEntity> getNewSamples(
            ChannelEntity channelEntity
    ) {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        SampleEntity localLastSample = sampleRepository.findLastByChannel(channelEntity);

        ChRequest chRequest = getServiceContainer()
                .getService(CommunicatorService.class)
                .getChRequestFactory(GetSamplesRequestFactory.class)
                .createRequest(
                        channelEntity,
                        (localLastSample == null) ? null : new Date(localLastSample.getEnd())
                );
        ChFuture chFuture = new ChFuture(chRequest);

        getServiceContainer().getService(CommunicatorService.class)
                .process(chFuture);
        try {
            ChResponse chResponse = chFuture.getChResponse();
            if (chResponse.getStatus() == ChResponse.Status.ERROR) {
                throw new RuntimeException();
            }
            JSONArray dataJA = (JSONArray) chResponse.getData();
            ArrayList<SampleEntity> sampleEntities = new ArrayList<>();
            for (int i = 0; i < dataJA.length(); i++) {
                sampleEntities.add(importSampleJSONObject(
                        dataJA.getJSONObject(i),
                        channelEntity
                ));
            }
            return sampleEntities;
        } catch (RuntimeException ex) {
            LOG.log(Level.FINE, "Exception during chRequest.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>();
    }

    public File getSampleFile(SampleEntity sampleEntity) {
        SampleStorageService sampleStorageService = getServiceContainer()
                .getService(SampleStorageService.class);

        if (sampleEntity.isLoaded()) {
            return sampleStorageService.getFile(sampleEntity);
        }

        ChRequest chRequest = getServiceContainer()
                .getService(CommunicatorService.class)
                .getChRequestFactory(GetSampleFileRequestFactory.class)
                .createRequest(sampleEntity);
        ChFuture chFuture = new ChFuture(chRequest);

        getServiceContainer().getService(CommunicatorService.class)
                .process(chFuture);
        try {
            ChResponse chResponse = chFuture.getChResponse();
            if (chResponse.getStatus() == ChResponse.Status.ERROR) {
                throw new RuntimeException();
            }
            File loadedSampleFile = new File(
                    tempDir.getAbsolutePath() + File.separator
                    + RandomStringGenerator.generate() + "." + sampleEntity.getExtension()
            );
            fileEncoder.decode((String) chResponse.getData(), loadedSampleFile);

            return getServiceContainer().getService(SampleStorageService.class)
                    .importSampleFile(sampleEntity, loadedSampleFile);
        } catch (RuntimeException ex) {
            LOG.log(Level.FINE, "Exception during chRequest.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public void clianupUnloaded() {
        getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .findAll()
                .forEach((ChannelEntity channelEntity) -> {
                    getFirstSample(channelEntity);
                });
    }

    /**
     * Cleanup all unrecorded samples before first sample loaded from remote
     * server. If sample couldn't be loaded returns local first sample or null.
     *
     * @param channelEntity
     * @return Could be null.
     */
    public SampleEntity getFirstSample(ChannelEntity channelEntity) {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        SampleEntity localFirstSample = sampleRepository.findFirstByChannel(channelEntity);

        ChRequest chRequest = getServiceContainer()
                .getService(CommunicatorService.class)
                .getChRequestFactory(GetFirstSampleRequestFactory.class)
                .createRequest(
                        channelEntity,
                        localFirstSample
                );
        ChFuture chFuture = new ChFuture(chRequest);

        getServiceContainer().getService(CommunicatorService.class)
                .process(chFuture);
        try {
            ChResponse chResponse = chFuture.getChResponse();
            if (chResponse.getStatus() == ChResponse.Status.ERROR) {
                throw new RuntimeException();
            }
            SampleEntity sampleEntity = importSampleJSONObject(
                    (JSONObject) chResponse.getData(),
                    channelEntity
            );
            sampleRepository.deleteUnloadedBefore(sampleEntity);
            return sampleEntity;
        } catch (RuntimeException ex) {
            LOG.log(Level.FINE, "Exception during chRequest.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return localFirstSample;
    }

    public void updateUnrecorded() {
        getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .findAll()
                .forEach((ChannelEntity channelEntity) -> {
                    checkUnrecorded(channelEntity);
                });
    }

    public List<SampleEntity> checkUnrecorded(
            ChannelEntity channelEntity
    ) {
        SampleRepository sampleRepository = (SampleRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        SampleStorageService sampleStorageService = getServiceContainer()
                .getService(SampleStorageService.class);

        List<SampleEntity> unrecordedSamples = sampleRepository.findUnrecorded(channelEntity, 50);
        if (!unrecordedSamples.isEmpty()) {
            ChRequest chRequest = getServiceContainer()
                    .getService(CommunicatorService.class)
                    .getChRequestFactory(CheckUnrecordedSamplesRequestFactory.class)
                    .createRequest(
                            channelEntity,
                            unrecordedSamples
                    );
            ChFuture chFuture = new ChFuture(chRequest);

            getServiceContainer().getService(CommunicatorService.class)
                    .process(chFuture);
            try {
                ChResponse chResponse = chFuture.getChResponse();
                if (chResponse.getStatus() == ChResponse.Status.ERROR) {
                    throw new RuntimeException();
                }
                JSONObject dataJO = (JSONObject) chResponse.getData();
                unrecordedSamples.forEach((SampleEntity sampleEntity) -> {
                    String sampleState = dataJO.getString(sampleEntity.getUuid().toString());
                    switch (sampleState) {
                        case "not_found":
                            sampleStorageService.delete(sampleEntity);
                            break;
                        case "recorded":
                            sampleEntity.setRecorded(true);
                            sampleRepository.getAbstractHibernateService().update(sampleEntity);
                            break;
                        case "unrecorded":
                            break;
                        default:
                            throw new RuntimeException("Unexpected sample state: " + sampleState);
                    }
                });
            } catch (RuntimeException ex) {
                LOG.log(Level.FINE, "Exception during chRequest.", ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return unrecordedSamples;
    }

}
