package com.videoweber.server.window.source_list;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SourceEntity;
import com.videoweber.server.entity.SourceRtspEntity;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.server.window.source_create.SourceCreateWindow;
import com.videoweber.server.window.source_edit.SourceEditWindow;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SourceListController extends WindowController {

    @FXML
    private TableView<ArrayList<String>> tableView;

    @Override
    public void onWindowInitialized() {

        getService(EventService.class).getListeners().add(
                new EntityEventListener(
                        (EntityEvent event) -> {
                            updateTable();
                        },
                        SourceEntity.class,
                        null,
                        null
                )
        );

        for (int i = 0; i < tableView.getColumns().size(); i++) {
            TableColumn<ArrayList<String>, String> col = (TableColumn<ArrayList<String>, String>) tableView.getColumns().get(i);
            final int colIndex = i;
            col.setCellValueFactory(colData -> {
                List<String> rowValues = colData.getValue();
                String cellValue;
                if (colIndex < rowValues.size()) {
                    cellValue = rowValues.get(colIndex);
                } else {
                    cellValue = "";
                }
                return new ReadOnlyStringWrapper(cellValue);
            });
        }

        updateTable();
    }

    private void updateTable() {
        tableView.getItems().clear();

        getService(HibernateService.class)
                .getRepository(SourceRtspEntity.class)
                .findAll()
                .forEach((SourceRtspEntity sourceEntity) -> {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(String.valueOf(sourceEntity.getId()));
                    row.add(sourceEntity.getTitle());
                    row.add(sourceEntity.getUri());
                    row.add(
                            (sourceEntity
                                    .getMediaType()
                                    .isCompatible(MediaType.VIDEO))
                            ? "да" : "нет"
                    );
                    row.add(
                            (sourceEntity
                                    .getMediaType()
                                    .isCompatible(MediaType.AUDIO))
                            ? "да" : "нет"
                    );
                    tableView.getItems().add(row);
                });
    }

    public void createSourceAction() {
        getService(WindowService.class).openWindow(SourceCreateWindow.class);
    }

    public void deleteSourceAction() {
        HibernateService hibernateService = getService(HibernateService.class);

        long deleteItemId;
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = Gui.createAlert(Alert.AlertType.WARNING);
            alert.setTitle("Не выбран элемент");
            alert.setHeaderText("Не выбран элемент для удаления.");
            alert.setContentText("Необходимо выбрать элемент для удаления.");
            alert.show();
            return;
        } else {
            deleteItemId = Long.valueOf(tableView
                    .getSelectionModel()
                    .getSelectedItem()
                    .get(0));
        }

        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            SourceEntity sourceEntity = session.get(
                    SourceEntity.class,
                    deleteItemId
            );
            List<ChannelEntity> channels = session
                    .createQuery("SELECT ce FROM ChannelEntity ce WHERE ce.videoSource = :source OR ce.audioSource = :source")
                    .setParameter("source", sourceEntity)
                    .list();
            if (!channels.isEmpty()) {
                String[] channelTitleArray = new String[channels.size()];
                for (int i = 0; i < channels.size(); i++) {
                    channelTitleArray[i] = channels.get(i).getTitle();
                }
                if (Gui.createAlert(
                        Alert.AlertType.CONFIRMATION,
                        "Источник данных используется",
                        "Некоторые каналы используют удаляемый источник данных. Продолжить удаление?",
                        "Некоторые каналы используют удаляемый источник данных. В случае его удаления, каналы будут отвязаны от него. При необходимости вы можете назначить каналам новые источники данных. Список каналов:\n    - " + String.join(" \n    - ", channelTitleArray)
                ).showAndWait().get() != ButtonType.OK) {
                    return;
                }
            }
            channels.stream().forEach((channel) -> {
                if (sourceEntity.equals(channel.getVideoSource())) {
                    channel.setVideoSource(null);
                } else {
                    channel.setAudioSource(null);
                }
            });
            session.delete(sourceEntity);
            session.flush();
        });

        getService(EventService.class).trigger(
                new EntityEvent(
                        SourceEntity.class,
                        EntityOperation.DELETE,
                        deleteItemId
                )
        );
    }

    public void editSourceAction() {
        long editItemId;
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Не выбран элемент для редактирования."
            ).show();
            return;
        } else {
            editItemId = Long.valueOf(tableView
                    .getSelectionModel()
                    .getSelectedItem()
                    .get(0));
        }

        getService(WindowService.class)
                .openWindow(SourceEditWindow.class, new Object[]{editItemId});
    }

}
