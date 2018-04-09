package com.videoweber.server.window.channel_archive;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author gda
 */
public class ChannelArchiveWindow extends Window {
    
    public ChannelArchiveWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setMaximized(true);
    }
    
}
