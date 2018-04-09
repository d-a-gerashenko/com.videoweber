package com.videoweber.server.window.effect_create;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EffectCreateWindow extends Window {

    public EffectCreateWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setResizable(false);
    }

}
