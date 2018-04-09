package com.videoweber.server.service.communicator_service.request.check_server_state;

import com.videoweber.internet.client.RequestFactory;
import com.videoweber.server.service.AppStateHolderService;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckServerStateFactory implements RequestFactory<CheckServerStateRequest> {

    private final AppStateHolderService appStateHolderService;

    public CheckServerStateFactory(AppStateHolderService appStateHolderService) {
        if (appStateHolderService == null) {
            throw new NullPointerException();
        }
        this.appStateHolderService = appStateHolderService;
    }

    public CheckServerStateRequest createRequest() {
        return new CheckServerStateRequest(appStateHolderService.getState());
    }

}
