package com.videoweber.server.service.communicator_service.request.add_channel_response;

import com.videoweber.internet.client.RequestFactory;
import com.videoweber.internet.client.channel_request.ChResponse;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class AddChannelResponseFactory implements RequestFactory<AddChannelResponseRequest> {

    public AddChannelResponseRequest createRequest(String requestUid, ChResponse chResponse) {
        if (requestUid == null || requestUid.isEmpty()) {
            throw new IllegalArgumentException();
        }

        JSONObject chResponseJO = new JSONObject();
        chResponseJO.put("status", chResponse.getStatus());
        chResponseJO.put("data", chResponse.getData());

        JSONObject dataJO = new JSONObject();
        dataJO.put("requestUid", requestUid);
        dataJO.put("data", chResponseJO.toString());

        return new AddChannelResponseRequest(dataJO);
    }

}
