package com.videoweber.server.service.communicator_service.request.check_server_state;

import com.videoweber.internet.client.Request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckServerStateRequest extends Request {

    public CheckServerStateRequest(String serverStateUid) {
        super("check_server_state", serverStateUid);
    }

}
