package com.videoweber.server.window.source_create;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SourceCreateWindow extends Window {

    public SourceCreateWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Добавление источника данных");
        stage.setResizable(false);
    }

}
