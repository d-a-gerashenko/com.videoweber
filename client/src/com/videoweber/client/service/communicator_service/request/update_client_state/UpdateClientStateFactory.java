package com.videoweber.client.service.communicator_service.request.update_client_state;

import com.videoweber.internet.client.RequestFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UpdateClientStateFactory implements RequestFactory<UpdateClientStateRequest> {

    public UpdateClientStateRequest createRequest() {
        return new UpdateClientStateRequest();
    }

}
