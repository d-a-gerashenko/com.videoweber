package com.videoweber.server.window.source_list;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SourceListWindow extends Window {

    public SourceListWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Список источников данных");
    }

}
