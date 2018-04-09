package com.videoweber.server.service.communicator_service.channel_request_processor;

import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestProcessor;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.repository.SampleRepository;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckUnrecordedSamplesRequestProcessor implements ChRequestProcessor {

    private final ChannelRepository channelRepository;
    private final SampleRepository sampleRepository;

    public CheckUnrecordedSamplesRequestProcessor(ChannelRepository channelRepository, SampleRepository sampleRepository) {
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
        JSONArray samplesJA = (JSONArray) chRequest.getData();
        JSONObject resultJO = new JSONObject();
        for (int i = 0; i < samplesJA.length(); i++) {
            UUID uuid = UUID.fromString(samplesJA.getString(i));
            SampleEntity sampleEntity = sampleRepository.get(uuid);
            String sampleState;
            if (sampleEntity == null) {
                sampleState = "not_found";
            } else {
                sampleState = (sampleEntity.isRecorded()) ? "recorded" : "unrecorded";
            }

            resultJO.put(
                    uuid.toString(),
                    sampleState
            );
        }

        return new ChResponse(ChResponse.Status.SUCCESS, resultJO);
    }

    @Override
    public String getCommand() {
        return "check_unrecorded_samples";
    }

}
