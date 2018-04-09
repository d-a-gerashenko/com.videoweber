package com.videoweber.client.service.communicator_service.request.check_client_state;

import com.videoweber.internet.client.Request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckClientStateRequest extends Request {

    public CheckClientStateRequest(String clientStateUid) {
        super("check_client_state", clientStateUid);
    }

}
