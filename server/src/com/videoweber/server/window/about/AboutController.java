package com.videoweber.server.window.about;

import com.videoweber.server.ServerApp;
import com.videoweber.lib.app.service.window_service.WindowController;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class AboutController extends WindowController {

    @FXML
    private Label appInfoLabel;

    @Override
    public void onWindowInitialized() {
        appInfoLabel.setText(ServerApp.versionInfo());
    }

    public void openLinkAction() {
        URI aboutLink;
        try {
            aboutLink = new URI("http://www.videoweber.com/");
        } catch (URISyntaxException ex) {
            throw new RuntimeException();
        }
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(aboutLink);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
