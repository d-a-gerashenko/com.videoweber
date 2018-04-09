package com.videoweber.client.service.communicator_service.request.check_channel_request;

import com.videoweber.internet.client.RequestFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckChannelRequestFactory implements RequestFactory<CheckChannelRequest> {

    public CheckChannelRequest createRequest(String requestUid) {
        if (requestUid == null) {
            throw new NullPointerException();
        }
        return new CheckChannelRequest(requestUid);
    }

}
