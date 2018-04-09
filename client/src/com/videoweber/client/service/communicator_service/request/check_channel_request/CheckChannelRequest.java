package com.videoweber.client.service.communicator_service.request.check_channel_request;

import com.videoweber.internet.client.Request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckChannelRequest extends Request {

    public CheckChannelRequest(Object data) {
        super("check_channel_request", data);
    }

}
