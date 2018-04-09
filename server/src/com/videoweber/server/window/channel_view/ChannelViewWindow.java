package com.videoweber.server.window.channel_view;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelViewWindow extends Window {

    public ChannelViewWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Просмотр канала в реальном времени");
        stage.setResizable(false);
    }


}
