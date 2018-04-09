package com.videoweber.client.service.communicator_service.request.channel_request_factories;

import com.videoweber.client.entity.SampleEntity;
import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetSampleFileRequestFactory implements ChRequestFactory {

    public ChRequest createRequest(SampleEntity sampleEntity) {
        return new ChRequest(
                sampleEntity.getChannel().getUuid().toString(),
                "get_sample_file",
                sampleEntity.getRemoteUuid().toString()
        );
    }

}
