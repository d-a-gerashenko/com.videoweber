package com.videoweber.server.service.communicator_service.request.check_server_state;

import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;
import com.videoweber.server.service.communicator_service.request.update_server_state.UpdateServerStateFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckServerStateProcessor implements ResponseProcessor<CheckServerStateRequest> {

    @Override
    public void process(CheckServerStateRequest request, Response response, RequestManager requestManager) {
        switch ((String)response.getData()) {
            case "equal":
                return;
            case "different":
                requestManager.processRequest(requestManager.getFactory(UpdateServerStateFactory.class).createRequest());
                break;
            default:
                throw new RuntimeException();
        }
        
    }

    @Override
    public Class<CheckServerStateRequest> getRequestClass() {
        return CheckServerStateRequest.class;
    }

}
