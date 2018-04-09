package com.videoweber.server.window.effect_list;

import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.EffectEntity;
import com.videoweber.server.entity.EffectRotateEntity;
import com.videoweber.server.repository.EffectRepository;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.server.window.effect_create.EffectCreateWindow;
import com.videoweber.server.window.effect_edit.EffectEditWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EffectListController extends WindowController {

    @FXML
    private TableView<ArrayList<String>> tableView;

    @FXML
    private Parent root;

    private final DataFormat SERIALIZED_MIME_TYPE = new DataFormat(String.valueOf(this.hashCode()));
    private ChannelEntity detachedChannelEntity;

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
                        EffectEntity.class,
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
        tableView.setRowFactory((TableView<ArrayList<String>> tv) -> {
            TableRow<ArrayList<String>> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE))) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    ArrayList<String> draggedRow = tableView.getItems().remove(draggedIndex);

                    int dropIndex;

                    if (row.isEmpty()) {
                        dropIndex = tableView.getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }

                    tableView.getItems().add(dropIndex, draggedRow);

                    event.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    event.consume();
                    onTableReordered();
                }
            });

            return row;
        });
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
                        "Эффекты канала \"%s\"",
                        detachedChannelEntity.getTitle()
                )
        );
        refresh();
    }

    private void refresh() {
        tableView.getItems().clear();

        EffectRepository repository = (EffectRepository) getService(HibernateService.class)
                .getRepository(EffectEntity.class);
        repository
                .findAllByChannel(detachedChannelEntity)
                .forEach((EffectEntity entity) -> {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(String.valueOf(entity.getId()));
                    row.add(getVerbouseEffectDescription(entity));
                    tableView.getItems().add(row);
                });
    }

    private String getVerbouseEffectDescription(EffectEntity effectEntity) {
        if (effectEntity instanceof EffectRotateEntity) {
            return "Поворот на " + ((EffectRotateEntity) effectEntity).getAngel() + "°";
        }
        throw new IllegalArgumentException();
    }

    public void onTableReordered() {
        EffectRepository repository = (EffectRepository) getService(HibernateService.class)
                .getRepository(EffectEntity.class);
        HashMap<Long, EffectEntity> effectEntities = new HashMap<>();
        repository.findAllByChannel(detachedChannelEntity).forEach((EffectEntity ee) -> {
            effectEntities.put(ee.getId(), ee);
        });

        ListIterator<ArrayList<String>> iterator = tableView.getItems().listIterator();
        while (iterator.hasNext()) {
            ArrayList<String> item = iterator.next();
            effectEntities.get(Long.valueOf(item.get(0))).setOrder(iterator.previousIndex());
        }
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            effectEntities.values().forEach((EffectEntity se) -> {
                session.merge(se);
            });
            session.flush();
        });

        getService(EventService.class).trigger(new EntityEvent(
                ChannelEntity.class,
                EntityOperation.UPDATE,
                detachedChannelEntity.getUuid()
        ));
    }

    public void addAction() {
        getService(WindowService.class)
                .openWindow(EffectCreateWindow.class,
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
                .openWindow(EffectEditWindow.class, new Object[]{editItemId});
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
            EffectEntity effectEntity = session.get(
                    EffectEntity.class,
                    deleteItemId
            );
            session.delete(effectEntity);
            session.flush();
        });

        getService(EventService.class).trigger(
                new EntityEvent(
                        EffectEntity.class,
                        EntityOperation.DELETE,
                        deleteItemId
                )
        );
    }
}
