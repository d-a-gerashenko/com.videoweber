package com.videoweber.client;

import com.videoweber.lib.common.ResourceManager;
import com.videoweber.lib.gui.Gui;
import java.awt.TrayIcon;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ClientGui extends Gui {

    private final String TITLE = "Клиент видеонаблюдения";
    private Stage splashScreenStage;

    @Override
    public TrayIcon createTrayIcon(TrayIcon defaultTrayIcon) {
        defaultTrayIcon.setToolTip(TITLE);
        return defaultTrayIcon;
    }

    @Override
    public void onAppStart(Stage primaryStage) {
        splashScreenStage = new Stage();
        splashScreenStage.initStyle(StageStyle.TRANSPARENT);
        splashScreenStage.setAlwaysOnTop(true);
        splashScreenStage.getIcons().add(new Image(
                ResourceManager
                .getResourceFile("com/videoweber/lib/app/icon.png")
                .toURI().toString()
        ));

        Pane root = new Pane();
        root.setStyle("-fx-background-color: rgba(255, 255, 255, 0.0);");

        Scene scene = new Scene(root, 640, 377);
        scene.getStylesheets().add(ResourceManager.getResourceFile("com/videoweber/client/splash_screen.css").toURI().toString());
        scene.setFill(Color.TRANSPARENT);
        splashScreenStage.setScene(scene);

        ProgressBar progressIndicator = new ProgressBar();
        progressIndicator.setPrefSize(640, 377);
        root.getChildren().add(progressIndicator);
        splashScreenStage.show();
    }

    @Override
    public void onAppRun(Stage primaryStage) {
        splashScreenStage.close();

        primaryStage.setTitle(TITLE);
//        try {
//            primaryStage.setScene(
//                    new Scene(
//                            FXMLLoader.load(
//                                    MainController.class.getResource("Main.fxml")
//                            )
//                    )
//            );
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//        primaryStage.show();
//        primaryStage.setAlwaysOnTop(true);
//        primaryStage.setAlwaysOnTop(false);
    }

    @Override
    public void onAppStop() {
    }

}
