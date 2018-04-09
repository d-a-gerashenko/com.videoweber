package com.videoweber.client.service.communicator_service.request.get_online_channels;

import com.videoweber.internet.client.RequestFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetOnlineChannelsRequestFactory implements RequestFactory<GetOnlineChannelsRequest> {

    public GetOnlineChannelsRequest createRequest() {
        return new GetOnlineChannelsRequest();
    }

}
