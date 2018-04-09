package com.videoweber.client.window.range_constructor;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class RangeConstructorWindow extends Window {

    public RangeConstructorWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Конструктор интервалов");
        stage.setResizable(false);
    }

}
