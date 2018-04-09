package com.videoweber.client.service.communicator_service.request.create_channel_request;

import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CreateChannelRequestProcessor implements ResponseProcessor<CreateChannelRequest> {

    @Override
    public void process(CreateChannelRequest request, Response response, RequestManager requestManager) {
       switch ((String)response.getData()) {
            case "request_created":
                return;
            default:
                throw new RuntimeException();
        }

    }

    @Override
    public Class<CreateChannelRequest> getRequestClass() {
        return CreateChannelRequest.class;
    }

}
