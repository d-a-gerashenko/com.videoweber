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
import java.util.UUID;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetLastFrameRequestProcessor implements ChRequestProcessor {

    private final ChannelRepository channelRepository;
    private final SampleRepository sampleRepository;
    private final StorageService storageService;
    private final Framer framer;
    private final FileEncoder fileEncoder;

    public GetLastFrameRequestProcessor(
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
        UUID channelId = UUID.fromString(chRequest.getChannelUid());
        ChannelEntity channel = channelRepository.get(channelId);
        if (channel == null) {
            throw new RuntimeException(String.format(
                    "Can't find channel: %s.",
                    channelId
            ));
        }
        
        Long after = null;
        if (chRequest.getData() != null) {
            after = Long.valueOf(chRequest.getData().toString());
        }
        
        SampleEntity sample = sampleRepository.findLastByChannel(channel);
        if (sample == null) {
            throw new RuntimeException("There are no samples on channel.");
        }
        
        if (after != null && sample.getBegin().compareTo(after) <= 0) {
            throw new RuntimeException("No new samples after specified value.");
        }
        
        Frame frame = framer.cutFrame(storageService.createSample(sample));
        
        JSONObject responseDataJO = new JSONObject();
        responseDataJO.put("date", frame.getDate().getTime());
        responseDataJO.put("file", fileEncoder.encode(frame.getFile()));
        
        return new ChResponse(ChResponse.Status.SUCCESS, responseDataJO);
    }

    @Override
    public String getCommand() {
        return "get_last_frame";
    }

}
