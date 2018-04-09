package com.videoweber.server.service.communicator_service.request.get_channel_requests;

import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;
import com.videoweber.internet.client.channel_request.ChRequest;
import com.videoweber.internet.client.channel_request.ChRequestManager;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.server.service.communicator_service.request.add_channel_response.AddChannelResponseFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetChannelRequestsProcessor implements ResponseProcessor<GetChannelRequestsRequest> {

    private final ChRequestManager chRequestManager;

    public GetChannelRequestsProcessor(ChRequestManager chRequestManager) {
        if (chRequestManager == null) {
            throw new NullPointerException();
        }
        this.chRequestManager = chRequestManager;
    }

    @Override
    public void process(GetChannelRequestsRequest request, Response response, RequestManager requestManager) {
        JSONArray dataJA = (JSONArray) response.getData();
        if (dataJA.length() == 0) {
            return;
        }
        for (int i = 0; i < dataJA.length(); i++) {
            JSONObject arrayItem = (JSONObject) dataJA.get(i);
            String chRequestUid = (String) arrayItem.get("requestUid");
            ChResponse chResponse;
            try {
                String jsonString = (String) arrayItem.get("data");
                JSONObject chRequestJO = new JSONObject(jsonString);
                String channelUid = (String) chRequestJO.get("channelUid");
                String command = (String) chRequestJO.get("command");
                Object data = (chRequestJO.has("data"))?chRequestJO.get("data"):null;
                ChRequest chRequest = new ChRequest(
                        channelUid,
                        command,
                        data
                );
                chResponse = chRequestManager.process(chRequest);
            } catch (Exception ex) {
                chResponse = new ChResponse(
                        ChResponse.Status.ERROR,
                        "Error during chResponse handling:" + ex
                );
            }
            requestManager.processRequest(
                    requestManager
                    .getFactory(AddChannelResponseFactory.class)
                    .createRequest(chRequestUid, chResponse)
            );
        }

        requestManager.processRequest(
                requestManager
                .getFactory(GetChannelRequestsFactory.class)
                .createRequest()
        );
    }

    @Override
    public Class<GetChannelRequestsRequest> getRequestClass() {
        return GetChannelRequestsRequest.class;
    }

}
