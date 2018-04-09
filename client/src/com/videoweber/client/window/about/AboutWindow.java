package com.videoweber.client.window.about;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class AboutWindow extends Window {

    public AboutWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("О разработчике");
        stage.setResizable(false);
    }

}
