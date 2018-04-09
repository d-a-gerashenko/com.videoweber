package com.videoweber.server.window.trigger_edit;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TriggerEditWindow extends Window {

    public TriggerEditWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setResizable(false);
    }

}
