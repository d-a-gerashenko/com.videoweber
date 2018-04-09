package com.videoweber.client.service.communicator_service.request.channel_request_factories;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestFactory;
import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetLastFrameRequestFactory implements ChRequestFactory {

    public ChRequest createRequest(ChannelEntity channelEntity, Date after) {
        return new ChRequest(
                channelEntity.getUuid().toString(),
                "get_last_frame",
                (after == null) ? null : after.getTime()
        );
    }

    public ChRequest createRequest(ChannelEntity channelEntity) {
        return createRequest(channelEntity, null);
    }

}
