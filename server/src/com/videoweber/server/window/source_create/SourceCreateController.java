package com.videoweber.server.window.source_create;

import com.videoweber.lib.channel.SourceProbe;
import com.videoweber.lib.channel.sources.Rtsp;
import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.engines.javacv.FfmpegSourceProbeFactory;
import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.SourceEntity;
import com.videoweber.server.entity.SourceRtspEntity;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
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
public class SourceCreateController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextField uriTextField;

    @FXML
    private ChoiceBox<MediaType> mediaTypeChoiceBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mediaTypeChoiceBox.setConverter(new StringConverter<MediaType>() {
            @Override
            public String toString(MediaType object) {
                return verbalMediaType.get(object);
            }

            @Override
            public MediaType fromString(String string) {
                for (Map.Entry<MediaType, String> entry : verbalMediaType.entrySet()) {
                    if (entry.getValue().equals(string)) {
                        return entry.getKey();
                    }
                }
                return null;
            }
        });
        mediaTypeChoiceBox.getItems().add(MediaType.VIDEO_AND_AUDIO);
        mediaTypeChoiceBox.getItems().add(MediaType.VIDEO);
        mediaTypeChoiceBox.getItems().add(MediaType.AUDIO);
    }

    private HashMap<MediaType, String> verbalMediaType = new HashMap<MediaType, String>() {
        {
            put(MediaType.AUDIO, "аудио");
            put(MediaType.VIDEO, "видео");
            put(MediaType.VIDEO_AND_AUDIO, "видео + аудио");
        }
    };

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    public void testAction() {
        try {
            validateData();
        } catch (DataValidationException ex) {
            return;
        }

        FfmpegSourceProbeFactory ffmpegSourceProbeFactory = new FfmpegSourceProbeFactory();
        SourceProbe probe = ffmpegSourceProbeFactory.createProbe(
                new Rtsp(
                        MediaType.VIDEO_AND_AUDIO,
                        uriTextField.getText()
                )
        );
        MediaType mediaType;
        try {
            mediaType = probe.getMediaType();
        } catch (Exception ex) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Ошибка при подключении к устройству",
                    "При подключении к устройсту произошла ошибка. Наиболее распространенной ошибкой является отсутствие соединения с устройством.",
                    ex.getLocalizedMessage()
            ).show();
            return;
        }
        mediaTypeChoiceBox.getSelectionModel().select(mediaType);

        Gui.createAlert(
                Alert.AlertType.INFORMATION,
                "Проверка устройства прошла успешно",
                String.format("Устройство имеет тип: \"%s\"", verbalMediaType.get(mediaType))
        ).show();
    }

    private void validateData() throws DataValidationException {
        if (titleTextField.getText().isEmpty()
                || titleTextField.getText().length() > 50) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Неправильный формат описания",
                    "Описание должно быть не пустым и иметь длину не более 50 символов."
            ).show();
            throw new DataValidationException();
        }
        if (!uriTextField
                .getText()
                .matches("^(rtsp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Неправильный формат RTSP адреса"
            ).show();
            throw new DataValidationException();
        }

        HibernateService hibernateService = getService(HibernateService.class);

        long conunt;
        conunt = getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            return (long) session
                    .createQuery("SELECT count(se) FROM SourceEntity se WHERE title = :title")
                    .setString("title", titleTextField.getText())
                    .uniqueResult();
        });
        if (conunt > 0) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Устройство с таким описанием уже существует."
            ).show();
            throw new DataValidationException();
        }

        conunt = getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            return (long) session
                    .createQuery("SELECT count(se) FROM SourceRtspEntity se WHERE uri = :uri")
                    .setString("uri", uriTextField.getText())
                    .uniqueResult();
        });
        if (conunt > 0) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Устройство с таким RTSP адресом уже существует."
            ).show();
            throw new DataValidationException();
        }
    }

    public void saveAction() {
        try {
            validateData();
        } catch (DataValidationException ex) {
            return;
        }

        if (mediaTypeChoiceBox.getValue() == null) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Не выбран тип данных"
            ).show();
            return;
        }

        SourceRtspEntity sourceRtspEntity = new SourceRtspEntity();
        sourceRtspEntity.setTitle(titleTextField.getText());
        sourceRtspEntity.setUri(uriTextField.getText());
        sourceRtspEntity.setMediaType(mediaTypeChoiceBox.getValue());

        getServiceContainer().getService(HibernateService.class).save(sourceRtspEntity);

        getService(EventService.class).trigger(
                new EntityEvent(
                        SourceEntity.class,
                        EntityOperation.CREATE,
                        sourceRtspEntity.getId()
                )
        );
        getStage().close();
        titleTextField.setText("");
        uriTextField.setText("");
        mediaTypeChoiceBox.setValue(null);
    }

}
