package com.videoweber.server.service.communicator_service.request.add_channel_response;

import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class AddChannelResponseProcessor implements ResponseProcessor<AddChannelResponseRequest> {

    @Override
    public void process(AddChannelResponseRequest request, Response response, RequestManager requestManager) {
        switch ((String)response.getData()) {
            case "responses_added":
                return;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public Class<AddChannelResponseRequest> getRequestClass() {
        return AddChannelResponseRequest.class;
    }

}
