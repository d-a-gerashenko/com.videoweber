package com.videoweber.server.service.communicator_service.channel_request_processor;

import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestProcessor;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.internet.client.channel_request.FileEncoder;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.service.storage_service.StorageService;
import java.io.File;
import java.util.UUID;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetSampleFileRequestProcessor implements ChRequestProcessor {

    private final SampleRepository sampleRepository;
    private final StorageService storageService;
    private final FileEncoder fileEncoder;

    public GetSampleFileRequestProcessor(SampleRepository sampleRepository, StorageService storageService, FileEncoder fileEncoder) {
        if (sampleRepository == null
                || storageService == null
                || fileEncoder == null) {
            throw new NullPointerException();
        }
        this.sampleRepository = sampleRepository;
        this.storageService = storageService;
        this.fileEncoder = fileEncoder;
    }

    @Override
    public ChResponse process(ChRequest chRequest) {
        UUID sampleUuid = UUID.fromString((String) chRequest.getData());
        SampleEntity sampleEntity = sampleRepository.get(sampleUuid);
        if (sampleEntity == null || sampleEntity.getDeleted() != null) {
            throw new RuntimeException(String.format(
                    "Sample %s entity not found.",
                    sampleUuid
            ));
        }
        File sampleFile = storageService.getFile(sampleEntity);
        return new ChResponse(ChResponse.Status.SUCCESS, fileEncoder.encode(sampleFile));
    }

    @Override
    public String getCommand() {
        return "get_sample_file";
    }

}
