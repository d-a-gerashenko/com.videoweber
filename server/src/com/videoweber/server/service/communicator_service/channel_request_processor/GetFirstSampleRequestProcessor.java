package com.videoweber.server.service.communicator_service.channel_request_processor;

import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestProcessor;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.repository.SampleRepository;
import java.util.UUID;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetFirstSampleRequestProcessor implements ChRequestProcessor {

    private final ChannelRepository channelRepository;
    private final SampleRepository sampleRepository;

    public GetFirstSampleRequestProcessor(
            ChannelRepository channelRepository,
            SampleRepository sampleRepository
    ) {
        if (channelRepository == null
                || sampleRepository == null) {
            throw new NullPointerException();
        }
        this.channelRepository = channelRepository;
        this.sampleRepository = sampleRepository;
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

        UUID exclude = null;
        if (chRequest.getData() != null) {
            exclude = UUID.fromString(chRequest.getData().toString());
        }

        SampleEntity sample = sampleRepository.findFirstByChannel(channel);
        if (sample == null) {
            throw new RuntimeException("There are no samples on channel.");
        }

        if (exclude != null && sample.getUuid().compareTo(exclude) == 0) {
            throw new RuntimeException("Sample is excluded.");
        }

        JSONObject responseDataJO = GetSamplesRequestProcessor.sampleToJSONObject(sample);

        return new ChResponse(ChResponse.Status.SUCCESS, responseDataJO);
    }

    @Override
    public String getCommand() {
        return "get_first_sample";
    }

}
