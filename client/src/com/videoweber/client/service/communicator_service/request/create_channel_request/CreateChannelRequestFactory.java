package com.videoweber.client.service.communicator_service.request.create_channel_request;

import com.videoweber.internet.client.RequestFactory;
import com.videoweber.internet.client.channel_request.ChRequest;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CreateChannelRequestFactory implements RequestFactory<CreateChannelRequest> {

    public CreateChannelRequest createRequest(String requestUid, ChRequest chRequest) {
        if (requestUid == null
                || chRequest == null) {
            throw new NullPointerException();
        }
        JSONObject chRequestJO = new JSONObject();
        chRequestJO.put("command", chRequest.getCommand());
        chRequestJO.put("channelUid", chRequest.getChannelUid());
        chRequestJO.put("data", chRequest.getData());
        
        JSONObject requestJO = new JSONObject();
        requestJO.put("channelUid", chRequest.getChannelUid());
        requestJO.put("uid", requestUid);
        requestJO.put("data", chRequestJO.toString());
        return new CreateChannelRequest(requestJO);
    }

}
