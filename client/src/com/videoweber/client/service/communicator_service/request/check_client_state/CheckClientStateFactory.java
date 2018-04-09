package com.videoweber.client.service.communicator_service.request.check_client_state;

import com.videoweber.client.service.AppStateHolderService;
import com.videoweber.internet.client.RequestFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CheckClientStateFactory implements RequestFactory<CheckClientStateRequest> {

    private final AppStateHolderService appStateHolderService;

    public CheckClientStateFactory(AppStateHolderService appStateHolderService) {
        if (appStateHolderService == null) {
            throw new NullPointerException();
        }
        this.appStateHolderService = appStateHolderService;
    }

    public CheckClientStateRequest createRequest() {
        return new CheckClientStateRequest(appStateHolderService.getState());
    }

}
