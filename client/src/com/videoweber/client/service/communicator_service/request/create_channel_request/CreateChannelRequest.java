package com.videoweber.client.service.communicator_service.request.create_channel_request;

import com.videoweber.internet.client.Request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CreateChannelRequest extends Request {

    public CreateChannelRequest(Object data) {
        super("create_channel_request", data);
    }

}
