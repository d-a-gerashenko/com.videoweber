package com.videoweber.client.service.communicator_service.request.check_channel_request;

import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;
import java.util.function.BiConsumer;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckChannelRequestProcessor implements ResponseProcessor<CheckChannelRequest> {

    private final BiConsumer<String, Response> handler;

    public CheckChannelRequestProcessor(BiConsumer<String, Response> handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        this.handler = handler;
    }

    @Override
    public void process(CheckChannelRequest request, Response response, RequestManager requestManager) {
        String requestUid = (String) request.getData();
        handler.accept(requestUid, response);
    }

    @Override
    public Class<CheckChannelRequest> getRequestClass() {
        return CheckChannelRequest.class;
    }

}
