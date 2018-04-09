package com.videoweber.server.window.source_edit;

import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.SourceEntity;
import com.videoweber.server.entity.SourceRtspEntity;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SourceEditController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private TextField titleTextField;

    private SourceRtspEntity sourceRtspEntity = null;

    @Override
    public void onWindowOpened(Object[] parameters) {
        if (parameters == null
                || parameters.length == 0
                || !(parameters[0] instanceof Long)) {
            throw new IllegalArgumentException();
        }
        Long sourceId = (Long) parameters[0];
        sourceRtspEntity = getService(HibernateService.class)
                .getRepository(SourceRtspEntity.class)
                .get(sourceId);
        if (sourceRtspEntity == null) {
            throw new NullPointerException();
        }
        titleTextField.setText(sourceRtspEntity.getTitle());
    }

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    public void saveAction() {
        if (titleTextField.getText().isEmpty()
                || titleTextField.getText().length() > 50) {
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
                    .createQuery("SELECT count(se) FROM SourceEntity se WHERE title = :title AND se <> :se")
                    .setString("title", titleTextField.getText())
                    .setParameter("se", sourceRtspEntity)
                    .uniqueResult();
        });
        if (conunt > 0) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Устройство с таким описанием уже существует."
            ).show();
            return;
        }
        sourceRtspEntity.setTitle(titleTextField.getText());
        getServiceContainer().getService(HibernateService.class).update(sourceRtspEntity);

        getService(EventService.class).trigger(
                new EntityEvent(
                        SourceEntity.class,
                        EntityOperation.UPDATE,
                        sourceRtspEntity.getId()
                )
        );
        getStage().close();
        titleTextField.setText("");
    }

}
