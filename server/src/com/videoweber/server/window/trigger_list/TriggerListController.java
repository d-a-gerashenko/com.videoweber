package com.videoweber.server.window.trigger_list;

import com.videoweber.lib.gui.Gui;
import com.videoweber.lib.recorder.triggers.MotionDetector;
import com.videoweber.lib.recorder.triggers.SoundDetector;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.TriggerEntity;
import com.videoweber.server.entity.TriggerMotionEntity;
import com.videoweber.server.entity.TriggerSoundEntity;
import com.videoweber.server.repository.TriggerRepository;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.server.window.trigger_create.TriggerCreateWindow;
import com.videoweber.server.window.trigger_edit.TriggerEditWindow;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TriggerListController extends WindowController {

    @FXML
    private TableView<ArrayList<String>> tableView;

    @FXML
    private Parent root;

    private ChannelEntity detachedChannelEntity;
    private DecimalFormat df = new DecimalFormat("#") {
        {
            setRoundingMode(RoundingMode.HALF_UP);
        }
    };

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void onWindowInitialized() {
        getService(EventService.class).getListeners().add(
                new EntityEventListener(
                        (EntityEvent event) -> {
                            refresh();
                        },
                        TriggerEntity.class,
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
                        "Условия записи канала \"%s\"",
                        detachedChannelEntity.getTitle()
                )
        );
        refresh();
    }

    private void refresh() {
        tableView.getItems().clear();

        TriggerRepository repository = (TriggerRepository) getService(HibernateService.class)
                .getRepository(TriggerEntity.class);
        repository
                .findAllByChannel(detachedChannelEntity)
                .forEach((TriggerEntity entity) -> {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(String.valueOf(entity.getId()));
                    row.add(String.valueOf(entity.getDurationBefore() / 1000));
                    row.add(String.valueOf(entity.getDurationAfter() / 1000));
                    row.add(getVerbouseTriggerDescription(entity));
                    tableView.getItems().add(row);
                });
    }

    private String getVerbouseTriggerDescription(TriggerEntity triggerEntity) {
        if (triggerEntity instanceof TriggerMotionEntity) {
            TriggerMotionEntity triggerMotionEntity = (TriggerMotionEntity) triggerEntity;
            return String.format(
                    "Детектор движения [%s,%s]",
                    df.format(
                            100 * (triggerMotionEntity.getThresholdMin() - MotionDetector.DEFAULT_THRESHOLD_MIN) / (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN)
                    ),
                    df.format(
                            100 * (triggerMotionEntity.getThresholdMax() - MotionDetector.DEFAULT_THRESHOLD_MIN) / (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN)
                    )
            );
        }
        if (triggerEntity instanceof TriggerSoundEntity) {
            TriggerSoundEntity triggerSoundEntity = (TriggerSoundEntity) triggerEntity;
            return String.format(
                    "Детектор звука (%s)",
                    df.format(
                            100 * (triggerSoundEntity.getThreshold() - SoundDetector.DEFAULT_THRESHOLD) / (0 - SoundDetector.DEFAULT_THRESHOLD)
                    )
            );
        }
        throw new IllegalArgumentException();
    }

    public void addAction() {
        getService(WindowService.class)
                .openWindow(TriggerCreateWindow.class,
                        new Object[]{detachedChannelEntity.getUuid()}
                );
    }

    public void editAction() {
        long editItemId;
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Не выбран элемент для редактирования"
            ).show();
            return;
        } else {
            editItemId = Long.valueOf(tableView
                    .getSelectionModel()
                    .getSelectedItem()
                    .get(0));
        }

        getService(WindowService.class)
                .openWindow(TriggerEditWindow.class, new Object[]{editItemId});
    }

    public void deleteAction() {
        HibernateService hibernateService = getService(HibernateService.class);

        long deleteItemId;
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Не выбран элемент для удаления"
            ).show();
            return;
        } else {
            deleteItemId = Long.valueOf(tableView
                    .getSelectionModel()
                    .getSelectedItem()
                    .get(0));
        }

        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            TriggerEntity triggerEntity = session.get(
                    TriggerEntity.class,
                    deleteItemId
            );
            session.delete(triggerEntity);
            session.flush();
        });

        getService(EventService.class).trigger(
                new EntityEvent(
                        TriggerEntity.class,
                        EntityOperation.DELETE,
                        deleteItemId
                )
        );
    }
}
