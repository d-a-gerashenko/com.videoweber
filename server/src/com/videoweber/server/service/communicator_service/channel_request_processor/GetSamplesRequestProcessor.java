package com.videoweber.server.service.communicator_service.channel_request_processor;

import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestProcessor;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.repository.SampleRepository;
import java.util.Date;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetSamplesRequestProcessor implements ChRequestProcessor {

    private final ChannelRepository channelRepository;
    private final SampleRepository sampleRepository;

    public GetSamplesRequestProcessor(ChannelRepository channelRepository, SampleRepository sampleRepository) {
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
        long after = 0;
        if (chRequest.getData() != null) {
            after = Long.valueOf(chRequest.getData().toString());
        } else {
            SampleEntity firstSample = sampleRepository.findFirstByChannel(channel);
            if (firstSample != null) {
                after = firstSample.getBegin();
            }
        }
        JSONArray samplesJA = new JSONArray();
        sampleRepository.findNextSamples(channel, new Date(after), 500)
                .forEach((SampleEntity sample) -> {
                    JSONObject sampleJO = sampleToJSONObject(sample);
                    samplesJA.put(sampleJO);
                });
        return new ChResponse(ChResponse.Status.SUCCESS, samplesJA);
    }

    public static JSONObject sampleToJSONObject(SampleEntity sampleEntity) {
        JSONObject sampleJO = new JSONObject();
        sampleJO.put("uid", sampleEntity.getUuid());
        sampleJO.put("samplerInfo", sampleEntity.getSamplerInfo());
        sampleJO.put("begin", sampleEntity.getBegin());
        sampleJO.put("extension", sampleEntity.getExtension());
        sampleJO.put("duration", sampleEntity.getDuration());
        sampleJO.put("size", sampleEntity.getSize());
        sampleJO.put("mediaType", sampleEntity.getMediaType());
        sampleJO.put("isRecorded", sampleEntity.isRecorded());
        return sampleJO;
    }

    @Override
    public String getCommand() {
        return "get_samples";
    }

}
