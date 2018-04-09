package com.videoweber.client.window.range_edit;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class RangeEditWindow extends Window {

    public RangeEditWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Редактирование интервала");
        stage.setResizable(false);
    }

}
