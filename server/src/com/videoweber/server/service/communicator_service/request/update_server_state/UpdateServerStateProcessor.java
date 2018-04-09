package com.videoweber.server.service.communicator_service.request.update_server_state;

import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UpdateServerStateProcessor implements ResponseProcessor<UpdateServerStateRequest> {

    @Override
    public void process(UpdateServerStateRequest request, Response response, RequestManager requestManager) {
        switch ((String)response.getData()) {
            case "server_updated":
                return;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public Class<UpdateServerStateRequest> getRequestClass() {
        return UpdateServerStateRequest.class;
    }

}
