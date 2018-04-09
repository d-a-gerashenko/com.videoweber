package com.videoweber.client.service.communicator_service.request.channel_request_factories;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestFactory;
import java.util.List;
import org.json.JSONArray;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckUnrecordedSamplesRequestFactory implements ChRequestFactory {

    public ChRequest createRequest(ChannelEntity channelEntity, List<SampleEntity> samples) {
        if (channelEntity == null
                || samples == null) {
            throw new NullPointerException();
        }
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Samples list shouldn't be empty.");
        }
        
        JSONArray dataJA = new JSONArray();
        samples.forEach((SampleEntity sampleEntity) -> {
            dataJA.put(sampleEntity.getUuid().toString());
        });
        
        return new ChRequest(
                channelEntity.getUuid().toString(),
                "check_unrecorded_samples",
                dataJA
        );
    }

}
