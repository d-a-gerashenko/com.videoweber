package com.videoweber.client.service.communicator_service.request.update_client_state;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.FrameEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.repository.FrameRepository;
import com.videoweber.client.repository.SampleRepository;
import com.videoweber.client.service.AppStateHolderService;
import com.videoweber.client.service.HibernateService;
import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.Response;
import com.videoweber.internet.client.ResponseProcessor;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UpdateClientStateProcessor implements ResponseProcessor<UpdateClientStateRequest> {

    private final HibernateService hibernateService;
    private final AppStateHolderService appStateHolderService;
    private final EventService eventService;

    public UpdateClientStateProcessor(
            HibernateService hibernateService,
            AppStateHolderService appStateHolderService,
            EventService eventService
    ) {
        if (hibernateService == null
                || appStateHolderService == null
                || eventService == null) {
            throw new NullPointerException();
        }
        this.hibernateService = hibernateService;
        this.appStateHolderService = appStateHolderService;
        this.eventService = eventService;
    }

    @Override
    public void process(UpdateClientStateRequest request, Response response, RequestManager requestManager) {
        JSONObject dataJO = (JSONObject) response.getData();
        String clientStateUid = dataJO.getString("state_uid");
        JSONArray channelsJA = dataJO.getJSONArray("channels");

        Repository<ChannelEntity> channelRepository = hibernateService.getRepository(ChannelEntity.class);
        FrameRepository frameRepository = (FrameRepository) hibernateService.getRepository(FrameEntity.class);
        SampleRepository sampleRepository = (SampleRepository) hibernateService.getRepository(SampleEntity.class);

        ArrayList<EntityEvent> entityEvents = new ArrayList<>();
        hibernateService.acquireSession((session) -> {
            Transaction tx = session.beginTransaction();
            try {
                List<UUID> newUUIDs = new ArrayList<>();
                for (int i = 0; i < channelsJA.length(); i++) {
                    JSONObject channelJO = channelsJA.getJSONObject(i);
                    UUID uuid = UUID.fromString(channelJO.getString("uid"));
                    String title = channelJO.getString("title");
                    Integer order = channelJO.getInt("order");
                    String path = channelJO.getString("path");
                    ChannelEntity channelEntity = channelRepository.get(uuid);

                    EntityOperation entityOperation = null;

                    if (channelEntity == null) {
                        channelEntity = new ChannelEntity();
                        channelEntity.setUuid(uuid);
                        session.persist(channelEntity);

                        entityOperation = EntityOperation.CREATE;
                    } else {
                        session.update(channelEntity);
                    }

                    if (!order.equals(channelEntity.getOrder())) {
                        channelEntity.setOrder(order);
                        if (entityOperation == null) {
                            entityOperation = EntityOperation.UPDATE;
                        }
                    }
                    if (!path.equals(channelEntity.getPath())) {
                        channelEntity.setPath(path);
                        if (entityOperation == null) {
                            entityOperation = EntityOperation.UPDATE;
                        }
                    }
                    if (!title.equals(channelEntity.getTitle())) {
                        channelEntity.setTitle(title);
                        if (entityOperation == null) {
                            entityOperation = EntityOperation.UPDATE;
                        }
                    }

                    if (entityOperation != null) {
                        entityEvents.add(new EntityEvent(
                                ChannelEntity.class,
                                entityOperation,
                                channelEntity.getUuid()
                        ));
                    }

                    newUUIDs.add(uuid);
                }

                channelRepository.findAll().forEach((ChannelEntity channelEntity) -> {
                    if (!newUUIDs.contains(channelEntity.getUuid())) {
                        frameRepository.deleteByChannel(channelEntity);
                        sampleRepository.deleteByChannel(channelEntity);
                        session.delete(channelEntity);
                        entityEvents.add(new EntityEvent(
                                ChannelEntity.class,
                                EntityOperation.DELETE,
                                channelEntity.getUuid()
                        ));
                    }
                });

                session.flush();
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                throw new RuntimeException(
                        "Can't update client state.",
                        e
                );
            }
        });
        appStateHolderService.setState(clientStateUid);
        entityEvents.forEach((EntityEvent entityEvent) -> eventService.trigger(entityEvent));
    }

    @Override
    public Class<UpdateClientStateRequest> getRequestClass() {
        return UpdateClientStateRequest.class;
    }

}
