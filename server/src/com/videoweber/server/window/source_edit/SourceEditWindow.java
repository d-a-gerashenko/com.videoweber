package com.videoweber.server.window.source_edit;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SourceEditWindow extends Window {

    public SourceEditWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Редактирование источника данных");
        stage.setResizable(false);
    }

}
