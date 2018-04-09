package com.videoweber.server.service.communicator_service.request.update_server_state;

import com.videoweber.internet.client.Request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UpdateServerStateRequest extends Request {

    public UpdateServerStateRequest(Object data) {
        super("update_server_state", data);
    }

}
