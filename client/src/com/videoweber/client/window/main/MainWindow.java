package com.videoweber.client.window.main;

import com.videoweber.lib.app.service.window_service.PrimaryStageWindow;
import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class MainWindow extends Window implements PrimaryStageWindow {

    public MainWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setMaximized(true);
    }
}
