package com.videoweber.server.service.communicator_service.request.add_channel_response;

import com.videoweber.internet.client.Request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class AddChannelResponseRequest extends Request {

    public AddChannelResponseRequest(Object data) {
        super("add_channel_response", data);
    }

}
