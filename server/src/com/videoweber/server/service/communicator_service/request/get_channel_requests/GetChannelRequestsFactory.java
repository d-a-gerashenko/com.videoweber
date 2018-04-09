package com.videoweber.server.service.communicator_service.request.get_channel_requests;

import com.videoweber.internet.client.RequestFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetChannelRequestsFactory implements RequestFactory<GetChannelRequestsRequest> {

    public GetChannelRequestsRequest createRequest() {
        return new GetChannelRequestsRequest();
    }

}
