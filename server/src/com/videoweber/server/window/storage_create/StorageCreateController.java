package com.videoweber.server.window.storage_create;

import com.videoweber.lib.gui.Gui;
import com.videoweber.server.entity.StorageEntity;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class StorageCreateController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private TextField directoryPathTextField;

    @FXML
    private TextField sizeTextField;

    @FXML
    private Label usableSpaceLabel;

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    public void chooseDirectoryAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выбор директории хранилища");
        File directory = directoryChooser.showDialog(getStage());
        if (directory != null) {
            directoryPathTextField.setText(directory.getAbsolutePath());
            usableSpaceLabel.setText(String.valueOf(directory.getUsableSpace() / 1024 / 1024));
        }
    }

    public void saveAction() {
        File directory = new File(directoryPathTextField.getText());
        Alert alert;
        if (!directory.exists()) {
            alert = Gui.createAlert(Alert.AlertType.WARNING);
            alert.setTitle("Не удается выполнить операцию");
            alert.setHeaderText("Заданная директория не существует.");
            alert.setContentText("Укажите существующую директорию.");
            alert.show();
            return;
        }
        HibernateService hibernateService = getService(HibernateService.class);

        long conunt = getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            return (long) session
                    .createQuery("SELECT count(se) FROM StorageEntity se WHERE _path = :path")
                    .setString("path", directory.getAbsolutePath())
                    .uniqueResult();
        });
        if (conunt > 0) {
            alert = Gui.createAlert(Alert.AlertType.WARNING);
            alert.setTitle("Директория уже используется");
            alert.setHeaderText("Заданная директория уже используется другим хранилищем.");
            alert.setContentText("Укажите другую директорию.");
            alert.show();
            return;
        }

        if (!directory.canWrite()) {
            alert = Gui.createAlert(Alert.AlertType.WARNING);
            alert.setTitle("Ограниченние доступа");
            alert.setHeaderText("Заданная директория не доступна для записи.");
            alert.setContentText("Укажите директорию с правами доступа на запись.");
            alert.show();
            return;
        }
        long sizeInBytes;
        try {
            sizeInBytes = Long.parseLong(sizeTextField.getText()) * 1024 * 1024;
        } catch (NumberFormatException nfe) {
            sizeInBytes = 0;
        }
        sizeTextField.setText(String.valueOf(sizeInBytes / 1024 / 1024));
        long usableSize = Long.valueOf(usableSpaceLabel.getText()) * 1024 * 1024;
        if (sizeInBytes <= 0 || sizeInBytes >= usableSize) {
            alert = Gui.createAlert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка размера хранилища");
            alert.setHeaderText("Неправильно указан размер хранилища.");
            alert.setContentText(String.format("Размер хранилища долежен лежать в диапазоне (0, %s).", usableSize / 1024 / 1024));
            alert.show();
            return;
        }
        StorageEntity newStorageEntity = new StorageEntity();
        newStorageEntity.setOrder(
                hibernateService
                        .getRepository(StorageEntity.class)
                        .findAll()
                        .size()
        );
        newStorageEntity.setPath(directory.getAbsolutePath());
        newStorageEntity.setSize(sizeInBytes);
        getServiceContainer().getService(HibernateService.class).save(newStorageEntity);
        getService(EventService.class).trigger(
                new EntityEvent(
                        StorageEntity.class,
                        EntityOperation.CREATE,
                        newStorageEntity.getId()
                )
        );

        getStage().close();
        directoryPathTextField.setText("");
        usableSpaceLabel.setText("---");
        sizeTextField.setText("");
    }
}
