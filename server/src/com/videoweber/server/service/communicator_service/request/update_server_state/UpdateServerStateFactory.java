package com.videoweber.server.service.communicator_service.request.update_server_state;

import com.videoweber.internet.client.RequestFactory;
import com.videoweber.lib.app.App;
import com.videoweber.server.service.AppStateHolderService;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.service.HibernateService;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UpdateServerStateFactory implements RequestFactory<UpdateServerStateRequest> {

    private final HibernateService hibernateService;
    private final AppStateHolderService appStateHolderService;
    
    public UpdateServerStateFactory(HibernateService hibernateService, AppStateHolderService appStateHolderService) {
        this.hibernateService = hibernateService;
        this.appStateHolderService = appStateHolderService;
    }

    public UpdateServerStateRequest createRequest() {
        JSONObject dataJO = new JSONObject();
        
        dataJO.put("version", App.versionInfo());
        dataJO.put("state_uid", appStateHolderService.getState());
        
        JSONArray channelsJA = new JSONArray();
        dataJO.put("channels", channelsJA);
        
        ChannelRepository channelRepository = (ChannelRepository)hibernateService.getRepository(ChannelEntity.class);
        channelRepository.findAll().forEach((ChannelEntity channelEntity) -> {
            JSONObject channelJO = new JSONObject();
            channelJO.put("uid", channelEntity.getUuid().toString());
            channelJO.put("title", channelEntity.getTitle());
            channelsJA.put(channelJO);
        });
                
        return new UpdateServerStateRequest(dataJO);
    }

}
