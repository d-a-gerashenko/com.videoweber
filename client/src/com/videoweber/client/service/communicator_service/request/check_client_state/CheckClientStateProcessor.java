package com.videoweber.client.service.communicator_service.request.check_client_state;

import com.videoweber.client.service.communicator_service.request.update_client_state.UpdateClientStateFactory;
import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckClientStateProcessor implements ResponseProcessor<CheckClientStateRequest> {

    @Override
    public void process(CheckClientStateRequest request, Response response, RequestManager requestManager) {
        switch ((String) response.getData()) {
            case "equal":
                return;
            case "different":
                requestManager.processRequest(requestManager.getFactory(UpdateClientStateFactory.class).createRequest());
                break;
            default:
                throw new RuntimeException();
        }

    }

    @Override
    public Class<CheckClientStateRequest> getRequestClass() {
        return CheckClientStateRequest.class;
    }

}
