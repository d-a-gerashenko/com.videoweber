package com.videoweber.server.window.effect_edit;

import com.videoweber.server.entity.EffectEntity;
import com.videoweber.server.entity.EffectRotateEntity;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import java.net.URL;
import java.util.ResourceBundle;
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
public class EffectEditController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private ChoiceBox<Integer> angelChoiceBox;

    @FXML
    private TabPane effectTabPane;

    private EffectEntity detachedEffectEntity;

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        angelChoiceBox.getItems().addAll(90, 180, 270);
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        if (parameters == null
                || parameters.length == 0
                || !(parameters[0] instanceof Long)) {
            throw new IllegalArgumentException();
        }
        long entityId = (long) parameters[0];
        detachedEffectEntity = getService(HibernateService.class)
                .getRepository(EffectEntity.class)
                .get(entityId);
        if (detachedEffectEntity == null) {
            throw new NullPointerException();
        }
        getStage().setTitle(
                String.format(
                        "Редактирование эффекта на канале \"%s\"",
                        detachedEffectEntity.getChannel().getTitle()
                )
        );

        if (detachedEffectEntity instanceof EffectRotateEntity) {
            EffectRotateEntity effectRotateEntity = (EffectRotateEntity) detachedEffectEntity;
            angelChoiceBox.getSelectionModel().select(Integer.valueOf(effectRotateEntity.getAngel()));
        } else {
            throw new RuntimeException("Unsupported effect: " + detachedEffectEntity.getClass().getName());
        }
    }

    @FXML
    public void saveAction() {
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            EffectEntity effectEntity;
            switch (effectTabPane.getSelectionModel().getSelectedIndex()) {
                case 0:
                    EffectRotateEntity effectRotateEntity = (EffectRotateEntity) detachedEffectEntity;
                    effectRotateEntity.setAngel(angelChoiceBox.getSelectionModel().getSelectedItem());
                    effectEntity = effectRotateEntity;
                    break;
                default:
                    throw new RuntimeException("Unsupported tab index:" + effectTabPane.getSelectionModel().getSelectedIndex());
            }
            session.merge(effectEntity);
            session.flush();
            getService(EventService.class).trigger(new EntityEvent(
                    EffectEntity.class,
                    EntityOperation.UPDATE,
                    effectEntity.getId()
            ));

            getStage().close();
        });

    }

}
