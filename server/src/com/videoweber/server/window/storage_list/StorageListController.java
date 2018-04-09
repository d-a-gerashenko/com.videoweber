package com.videoweber.server.window.storage_list;

import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.StorageEntity;
import com.videoweber.server.repository.StorageRepository;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.server.window.storage_create.StorageCreateWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class StorageListController extends WindowController {

    @FXML
    private TableView<ArrayList<String>> tableView;

    private final DataFormat SERIALIZED_MIME_TYPE = new DataFormat(String.valueOf(this.hashCode()));

    @Override
    public void onWindowInitialized() {
        getService(EventService.class).getListeners().add(
                new EntityEventListener(
                        (EntityEvent event) -> {
                            updateTable();
                        },
                        StorageEntity.class,
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

        updateTable();
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        updateTable();
    }

    public void updateTable() {
        tableView.getItems().clear();

        getService(HibernateService.class)
                .getRepository(StorageEntity.class)
                .findAll()
                .forEach((StorageEntity storageEntity) -> {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(String.valueOf(storageEntity.getId()));
                    row.add(String.valueOf(storageEntity.getSize() / 1024 / 1024));
                    row.add(String.valueOf(storageEntity.getSizeUsed() / 1024 / 1024));
                    row.add(String.valueOf((storageEntity.getSize() - storageEntity.getSizeUsed()) / 1024 / 1024));
                    row.add(storageEntity.getPath());
                    tableView.getItems().add(row);
                });
    }

    public void onTableReordered() {
        StorageRepository repository = (StorageRepository) getService(HibernateService.class)
                .getRepository(StorageEntity.class);
        HashMap<Long, StorageEntity> storageEntities = new HashMap<>();
        repository.findAll().forEach((StorageEntity se) -> {
            storageEntities.put(se.getId(), se);
        });

        ListIterator<ArrayList<String>> iterator = tableView.getItems().listIterator();
        while (iterator.hasNext()) {
            ArrayList<String> item = iterator.next();
            storageEntities.get(Long.valueOf(item.get(0))).setOrder(iterator.previousIndex());
        }
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            storageEntities.values().forEach((StorageEntity se) -> {
                session.merge(se);
            });
            session.flush();
        });
    }

    public void createStorageAction() {
        getService(WindowService.class).openWindow(StorageCreateWindow.class);
    }

    public void deleteStorageAction() {
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
            StorageEntity storageEntity = session.get(
                    StorageEntity.class,
                    deleteItemId
            );
            long conunt = (long) session
                    .createQuery("SELECT count(se) FROM SampleEntity se WHERE storage = :storage")
                    .setParameter("storage", storageEntity)
                    .uniqueResult();
            if (conunt > 0) {
                Alert alert = Gui.createAlert(Alert.AlertType.WARNING);
                alert.setTitle("Хранилище используется");
                alert.setHeaderText("Нельзя удалить используемое хранилище.");
                alert.setContentText("В хранилище уже содержатся данные, его нельзя удалить. В следующих версиях программы будет добавлено расформирование хранилищ.");
                alert.show();
                return;
            }
            session.delete(storageEntity);
            session.flush();
        });
        getService(EventService.class).trigger(
                new EntityEvent(
                        StorageEntity.class,
                        EntityOperation.DELETE,
                        deleteItemId
                )
        );
    }

}
