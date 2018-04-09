package com.videoweber.server.window.storage_create;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class StorageCreateWindow extends Window {

    public StorageCreateWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Создание нового хранилища");
        stage.setResizable(false);
    }

}
