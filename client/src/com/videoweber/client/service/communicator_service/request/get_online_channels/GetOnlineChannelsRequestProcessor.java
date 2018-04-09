package com.videoweber.client.service.communicator_service.request.get_online_channels;

import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.json.JSONArray;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GetOnlineChannelsRequestProcessor implements ResponseProcessor<GetOnlineChannelsRequest> {

    private final Consumer<Set<UUID>> handler;

    public GetOnlineChannelsRequestProcessor(Consumer<Set<UUID>> handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        this.handler = handler;
    }

    @Override
    public void process(GetOnlineChannelsRequest request, Response response, RequestManager requestManager) {
        JSONArray dataJA = (JSONArray) response.getData();
        HashSet<UUID> onlineChannels = new HashSet<>();
        for (int i = 0; i < dataJA.length(); i++) {
            UUID onlineChannelUuid = UUID.fromString(dataJA.getString(i));
            onlineChannels.add(onlineChannelUuid);
        }
        handler.accept(onlineChannels);
    }

    @Override
    public Class<GetOnlineChannelsRequest> getRequestClass() {
        return GetOnlineChannelsRequest.class;
    }

}
