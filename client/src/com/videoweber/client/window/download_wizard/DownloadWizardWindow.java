package com.videoweber.client.window.download_wizard;

import com.videoweber.lib.app.service.window_service.Window;
import com.videoweber.lib.app.service.window_service.WindowController;
import javafx.stage.Stage;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class DownloadWizardWindow extends Window {

    public DownloadWizardWindow(Stage stage, WindowController controller) {
        super(stage, controller);
        stage.setTitle("Загрузка данных");
    }

}
