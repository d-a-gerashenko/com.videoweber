package com.videoweber.client.service.communicator_service.request.channel_request_factories;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetFirstSampleRequestFactory implements ChRequestFactory {
    
    public ChRequest createRequest(ChannelEntity channelEntity, SampleEntity excluded) {
        return new ChRequest(
                channelEntity.getUuid().toString(),
                "get_first_sample",
                (excluded == null) ? null : excluded.getUuid().toString()
        );
    }
    
    public ChRequest createRequest(ChannelEntity channelEntity) {
        return createRequest(channelEntity, null);
    }
    
    public ChRequest createRequest(SampleEntity excluded) {
        return createRequest(excluded.getChannel(), excluded);
    }
    
}
