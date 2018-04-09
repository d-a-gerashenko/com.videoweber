package com.videoweber.server.window.channel_edit;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SourceEntity;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelEditController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private TextField channelTitleTextField;

    @FXML
    private ChoiceBox<SourceEntity> videoSourceChoiceBox;

    @FXML
    private ChoiceBox<SourceEntity> audioSourceChoiceBox;

    private ChannelEntity detachedChannelEntity;
    private List<SourceEntity> sourceEntities;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        videoSourceChoiceBox.setConverter(new StringConverter<SourceEntity>() {
            @Override
            public String toString(SourceEntity object) {
                if (object == null) {
                    return "---";
                }
                return String.format(
                        "#%s: %s",
                        object.getId(),
                        object.getTitle()
                );
            }

            @Override
            public SourceEntity fromString(String string) {
                int selectedIndex = videoSourceChoiceBox.getSelectionModel().getSelectedIndex();
                if (selectedIndex == 0) {
                    return null;
                }
                return videoSourceChoiceBox.getItems().get(selectedIndex - 1);
            }
        });
        audioSourceChoiceBox.setConverter(new StringConverter<SourceEntity>() {
            @Override
            public String toString(SourceEntity object) {
                if (object == null) {
                    return "---";
                }
                return String.format(
                        "#%s: %s",
                        object.getId(),
                        object.getTitle()
                );
            }

            @Override
            public SourceEntity fromString(String string) {
                int selectedIndex = audioSourceChoiceBox.getSelectionModel().getSelectedIndex();
                if (selectedIndex == 0) {
                    return null;
                }
                return audioSourceChoiceBox.getItems().get(selectedIndex - 1);
            }
        });
    }

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
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
                        "Редактирование канала \"%s\"",
                        detachedChannelEntity.getTitle()
                )
        );

        // filling forms
        channelTitleTextField.setText(detachedChannelEntity.getTitle());

        sourceEntities = getService(HibernateService.class)
                .getRepository(SourceEntity.class).findAll();

        videoSourceChoiceBox.getItems().clear();
        videoSourceChoiceBox.getItems().add(null);
        audioSourceChoiceBox.getItems().clear();
        audioSourceChoiceBox.getItems().add(null);
        sourceEntities.forEach((SourceEntity sourceEntity) -> {
            if (sourceEntity.getMediaType().isCompatible(MediaType.VIDEO)) {
                videoSourceChoiceBox.getItems().add(sourceEntity);
            }
            if (sourceEntity.getMediaType().isCompatible(MediaType.AUDIO)) {
                audioSourceChoiceBox.getItems().add(sourceEntity);
            }
        });

        if (detachedChannelEntity.getVideoSource() != null) {
            sourceEntities.stream()
                    .filter(
                            (sourceEntity) -> (sourceEntity.getId().compareTo(detachedChannelEntity.getVideoSource().getId()) == 0)
                    )
                    .forEach((sourceEntity) -> {
                        videoSourceChoiceBox.getSelectionModel().select(sourceEntity);
                    });
        }
        if (detachedChannelEntity.getAudioSource() != null) {
            sourceEntities.stream()
                    .filter(
                            (sourceEntity) -> (sourceEntity.getId().compareTo(detachedChannelEntity.getAudioSource().getId()) == 0)
                    )
                    .forEach((sourceEntity) -> {
                        audioSourceChoiceBox.getSelectionModel().select(sourceEntity);
                    });
        }
    }

    public void saveAction() {
        if (channelTitleTextField.getText().isEmpty()
                || channelTitleTextField.getText().length() > 50) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Неправильный формат описания",
                    "Описание должно быть не пустым и иметь длину не более 50 символов."
            ).show();
            return;
        }

        HibernateService hibernateService = getService(HibernateService.class);
        long conunt = getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            return (long) session
                    .createQuery("SELECT count(ce) FROM ChannelEntity ce WHERE title = :title AND ce <> :ce")
                    .setString("title", channelTitleTextField.getText())
                    .setParameter("ce", detachedChannelEntity)
                    .uniqueResult();
        });
        if (conunt > 0) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Канал с таким описанием уже существует."
            ).show();
            return;
        }

        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            ChannelEntity channelEntity = (ChannelEntity) session.merge(detachedChannelEntity);
            channelEntity.setTitle(channelTitleTextField.getText());

            SourceEntity detachedVideoSourceEntity = videoSourceChoiceBox.getSelectionModel().getSelectedItem();
            if (detachedVideoSourceEntity != null) {
                SourceEntity videoSourceEntity = (SourceEntity) session.merge(detachedVideoSourceEntity);
                channelEntity.setVideoSource(videoSourceEntity);
            } else {
                channelEntity.setVideoSource(null);
            }
            SourceEntity detachedAudioSourceEntity = audioSourceChoiceBox.getSelectionModel().getSelectedItem();
            if (detachedAudioSourceEntity != null) {
                SourceEntity audioSourceEntity = (SourceEntity) session.merge(detachedAudioSourceEntity);
                channelEntity.setAudioSource(audioSourceEntity);
            } else {
                channelEntity.setAudioSource(null);
            }

            session.flush();
        });

        getService(EventService.class).trigger(
                new EntityEvent(
                        ChannelEntity.class,
                        EntityOperation.UPDATE,
                        detachedChannelEntity.getUuid()
                )
        );
        getStage().close();
    }

}
