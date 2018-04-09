package com.videoweber.client.service.communicator_service;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.repository.ChannelRepository;
import com.videoweber.client.service.AppStateHolderService;
import com.videoweber.client.service.HibernateService;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.CheckUnrecordedSamplesRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetFirstSampleRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetFrameRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetLastFrameRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetSampleFileRequestFactory;
import com.videoweber.client.service.communicator_service.request.channel_request_factories.GetSamplesRequestFactory;
import com.videoweber.client.service.communicator_service.request.check_channel_request.CheckChannelRequestFactory;
import com.videoweber.client.service.communicator_service.request.check_channel_request.CheckChannelRequestProcessor;
import com.videoweber.client.service.communicator_service.request.check_client_state.CheckClientStateFactory;
import com.videoweber.client.service.communicator_service.request.check_client_state.CheckClientStateProcessor;
import com.videoweber.client.service.communicator_service.request.create_channel_request.CreateChannelRequestFactory;
import com.videoweber.client.service.communicator_service.request.create_channel_request.CreateChannelRequestProcessor;
import com.videoweber.client.service.communicator_service.request.get_online_channels.GetOnlineChannelsRequest;
import com.videoweber.client.service.communicator_service.request.get_online_channels.GetOnlineChannelsRequestFactory;
import com.videoweber.client.service.communicator_service.request.get_online_channels.GetOnlineChannelsRequestProcessor;
import com.videoweber.client.service.communicator_service.request.update_client_state.UpdateClientStateFactory;
import com.videoweber.client.service.communicator_service.request.update_client_state.UpdateClientStateProcessor;
import com.videoweber.internet.client.Client;
import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.channel_request.ChFuture;
import com.videoweber.internet.client.channel_request.ChRequestFactory;
import com.videoweber.internet.client.channel_request.ChRequestFactoryManager;
import com.videoweber.internet.client.channel_request.ChResponse;
import com.videoweber.lib.app.service.ParametersService;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.common.RandomStringGenerator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CommunicatorService extends Service {

    private static final Logger LOG = Logger.getLogger(CommunicatorService.class.getName());

    private final RequestManager requestManager;
    private final Map<String, ChFuture> chFutures = Collections.synchronizedMap(new HashMap());
    private final ChRequestFactoryManager chRequestFactoryManager;
    private final Set<UUID> onlineChannels = Collections.synchronizedSet(new HashSet<>());
    private final CountDownLatch releaseLatch = new CountDownLatch(1);

    public CommunicatorService(ServiceContainer serviceContainer) {
        super(serviceContainer);

        final ServiceContainer sc = getServiceContainer();

        Client client = new Client(
                sc.getService(ParametersService.class).getProperty("INTERNET_SERVER_URL"),
                sc.getService(ParametersService.class).getProperty("INTERNET_SERVER_KEY")
        );

        // RequestManager initialization...
        requestManager = new RequestManager(client);

        requestManager.registerProcessor(new CheckChannelRequestProcessor((String requestUid, Response response) -> {
            JSONObject dataJO = (JSONObject) response.getData();
            String state = dataJO.getString("state");
            switch (state) {
                case "waiting":
                    return;
                case "not_found":
                    requestManager.processRequest(
                            requestManager.getFactory(CreateChannelRequestFactory.class)
                                    .createRequest(
                                            requestUid,
                                            chFutures
                                                    .get(requestUid)
                                                    .getChRequest()
                                    )
                    );
                    return;
                case "ready":
                    JSONObject chResponseJO = new JSONObject(dataJO.getString("data"));
                    ChResponse.Status chResponseStatus = ChResponse.Status.valueOf(chResponseJO.getString("status"));
                    Object chResponseData = chResponseJO.get("data");
                    ChResponse chResponse = new ChResponse(chResponseStatus, chResponseData);
                    chFutures.get(requestUid).setChResponse(chResponse);
                    return;
                default:
                    throw new RuntimeException("Unexpected state: " + state);
            }
        }));
        requestManager.registerFactory(new CheckChannelRequestFactory());

        requestManager.registerProcessor(new CreateChannelRequestProcessor());
        requestManager.registerFactory(new CreateChannelRequestFactory());

        requestManager.registerProcessor(new CheckClientStateProcessor());
        requestManager.registerFactory(new CheckClientStateFactory(sc.getService(AppStateHolderService.class)));

        requestManager.registerProcessor(new UpdateClientStateProcessor(
                sc.getService(HibernateService.class),
                sc.getService(AppStateHolderService.class),
                sc.getService(EventService.class)
        ));
        requestManager.registerFactory(new UpdateClientStateFactory());

        requestManager.registerProcessor(new GetOnlineChannelsRequestProcessor((Set<UUID> onlineChannelUuids) -> {
            EventService eventService = getServiceContainer().getService(EventService.class);
            getServiceContainer()
                    .getService(HibernateService.class)
                    .getRepository(ChannelEntity.class)
                    .findAll()
                    .forEach((ChannelEntity channelEntity) -> {
                        UUID channelUuid = channelEntity.getUuid();
                        if (onlineChannels.contains(channelUuid)
                                && !onlineChannelUuids.contains(channelUuid)) {
                            onlineChannels.remove(channelUuid);
                            eventService
                                    .trigger(
                                            new ChannelOnlineStatusChangedEvent(
                                                    ChannelOnlineStatusChangedEvent.Status.OFFLINE,
                                                    channelUuid
                                            )
                                    );
                        }
                        if (!onlineChannels.contains(channelUuid)
                                && onlineChannelUuids.contains(channelUuid)) {
                            onlineChannels.add(channelUuid);
                            eventService
                                    .trigger(
                                            new ChannelOnlineStatusChangedEvent(
                                                    ChannelOnlineStatusChangedEvent.Status.ONLINE,
                                                    channelUuid
                                            )
                                    );
                        }
                    });
        }));
        requestManager.registerFactory(new GetOnlineChannelsRequestFactory());

        // ChRequest factories initialization...
        chRequestFactoryManager = new ChRequestFactoryManager();

        chRequestFactoryManager.registerFactory(new GetFrameRequestFactory());
        chRequestFactoryManager.registerFactory(new GetLastFrameRequestFactory());
        chRequestFactoryManager.registerFactory(new GetSampleFileRequestFactory());
        chRequestFactoryManager.registerFactory(new GetSamplesRequestFactory());
        chRequestFactoryManager.registerFactory(new CheckUnrecordedSamplesRequestFactory());
        chRequestFactoryManager.registerFactory(new GetFirstSampleRequestFactory());
    }

    public void executeConnection() {
        ChannelRepository channelRepository = (ChannelRepository) getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(ChannelEntity.class);
        try {
            requestManager.processRequest(requestManager.getFactory(CheckClientStateFactory.class).createRequest());
            requestManager.processRequest(requestManager.getFactory(GetOnlineChannelsRequestFactory.class).createRequest());
            synchronized (chFutures) {
                Iterator<Map.Entry<String, ChFuture>> it = chFutures.entrySet().iterator();

                for (int step = 0; step < 5 && it.hasNext(); step++) {
                    Map.Entry<String, ChFuture> entry = it.next();
                    String requestUid = entry.getKey();
                    ChFuture chFuture = entry.getValue();
                    UUID channelUuid = UUID.fromString(chFuture.getChRequest().getChannelUid());
                    
                    if (!onlineChannels.contains(channelUuid)) {
                        chFuture.setChResponse(new ChResponse(ChResponse.Status.ERROR, "Channel is offline."));
                    }
                    
                    if (chFuture.isDone()
                            || channelRepository.get(channelUuid) == null) {
                        it.remove();
                        step--;
                        continue;
                    }
                    if (!onlineChannels.contains(channelUuid)) {
                        step--;
                        continue;
                    }
                    requestManager.processRequest(requestManager.getFactory(CheckChannelRequestFactory.class).createRequest(
                            requestUid
                    ));
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error during connection to internet server.", ex);
        }
    }

    public String process(ChFuture chFuture) {
        if (chFuture == null) {
            throw new NullPointerException();
        }
        String requestUid;
        synchronized (chFutures) {
            if (releaseLatch.getCount() == 0) {
                throw new RuntimeException("CommunicationService is released.");
            }
            if (chFutures.containsValue(chFuture)) {
                throw new RuntimeException("ChFuture already added.");
            }
            requestUid = RandomStringGenerator.generate();
            chFutures.put(requestUid, chFuture);
        }
        return requestUid;
    }

    public ChRequestFactoryManager getChRequestFactoryManager() {
        return chRequestFactoryManager;
    }

    public <T extends ChRequestFactory> T getChRequestFactory(Class<T> chRequestFactoryClass) {
        return chRequestFactoryManager.getFactory(chRequestFactoryClass);
    }

    public Set<UUID> getOnlineChannels() {
        return new HashSet(onlineChannels);
    }

    public void release() {
        synchronized (chFutures) {
            releaseLatch.countDown();
            chFutures.values().forEach((chFuture) -> {
                chFuture.cancel();
            });
            chFutures.clear();
        }
    }

}
