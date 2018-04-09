package com.videoweber.server.service.communicator_service.channel_request_processor;

import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestProcessor;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.internet.client.channel_request.FileEncoder;
import com.videoweber.lib.framer.Frame;
import com.videoweber.lib.framer.Framer;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.service.storage_service.StorageService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetFrameRequestProcessor implements ChRequestProcessor {

    private final ChannelRepository channelRepository;
    private final SampleRepository sampleRepository;
    private final StorageService storageService;
    private final Framer framer;
    private final FileEncoder fileEncoder;

    public GetFrameRequestProcessor(
            ChannelRepository channelRepository,
            SampleRepository sampleRepository,
            StorageService storageService,
            Framer framer,
            FileEncoder fileEncoder
    ) {
        if (channelRepository == null
                || sampleRepository == null
                || framer == null
                || fileEncoder == null) {
            throw new NullPointerException();
        }
        this.channelRepository = channelRepository;
        this.sampleRepository = sampleRepository;
        this.storageService = storageService;
        this.framer = framer;
        this.fileEncoder = fileEncoder;
    }

    @Override
    public ChResponse process(ChRequest chRequest) {
        UUID sampleId = UUID.fromString((String)chRequest.getData());

        SampleEntity sample = sampleRepository.get(sampleId);
        
        if (sample == null) {
            throw new RuntimeException(String.format(
                    "There is no sample with id \"%s\".",
                    sampleId
            ));
        }
        
        Frame frame = framer.cutFrame(storageService.createSample(sample));

        return new ChResponse(ChResponse.Status.SUCCESS, fileEncoder.encode(frame.getFile()));
    }

    @Override
    public String getCommand() {
        return "get_frame";
    }

}
