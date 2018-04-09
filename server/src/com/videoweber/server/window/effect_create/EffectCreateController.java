package com.videoweber.server.window.effect_create;

import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.EffectEntity;
import com.videoweber.server.entity.EffectRotateEntity;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EffectCreateController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private ChoiceBox<Integer> angelChoiceBox;

    @FXML
    private TabPane effectTabPane;

    private ChannelEntity detachedChannelEntity;

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        angelChoiceBox.getItems().addAll(90, 180, 270);
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        angelChoiceBox.getSelectionModel().selectFirst();

        if (parameters == null
                || parameters.length == 0
                || !(parameters[0] instanceof UUID)) {
            throw new IllegalArgumentException();
        }
        UUID entityId = (UUID) parameters[0];
        detachedChannelEntity = getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .get(entityId);
        if (detachedChannelEntity == null) {
            throw new NullPointerException();
        }
        getStage().setTitle(
                String.format(
                        "Добавление эффекта на канал \"%s\"",
                        detachedChannelEntity.getTitle()
                )
        );
    }

    public void saveAction() {
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            EffectEntity effectEntity;
            ChannelEntity channelEntity = session.get(ChannelEntity.class, detachedChannelEntity.getUuid());
            int conunt = ((Long) session
                    .createQuery("SELECT count(ee) FROM EffectEntity ee WHERE channel = :channel")
                    .setParameter("channel", channelEntity)
                    .uniqueResult()).intValue();

            switch (effectTabPane.getSelectionModel().getSelectedIndex()) {
                case 0:
                    EffectRotateEntity effectRotateEntity = new EffectRotateEntity();
                    effectRotateEntity.setAngel(angelChoiceBox.getSelectionModel().getSelectedItem());
                    effectRotateEntity.setOrder(conunt);
                    effectEntity = effectRotateEntity;
                    break;
                default:
                    throw new RuntimeException("Unsupported tab index:" + effectTabPane.getSelectionModel().getSelectedIndex());
            }
            session.persist(effectEntity);
            effectEntity.setChannel(channelEntity);
            session.flush();
            getService(EventService.class).trigger(new EntityEvent(
                    EffectEntity.class,
                    EntityOperation.CREATE,
                    effectEntity.getId()
            ));

            getService(EventService.class).trigger(new EntityEvent(
                    ChannelEntity.class,
                    EntityOperation.UPDATE,
                    detachedChannelEntity.getUuid()
            ));
        });

        getStage().close();
    }

}
