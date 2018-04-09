package com.videoweber.lib.gui;

import com.sun.javafx.stage.StageHelper;
import com.videoweber.lib.JavaFX.JavaFxSynchronous;
import com.videoweber.lib.app.App;
import com.videoweber.lib.common.ResourceManager;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class Gui extends Application {

    private static Gui INSTANCE = null;
    private static final Logger LOG = Logger.getLogger(Gui.class.getName());
    private static final CountDownLatch START_LATCH = new CountDownLatch(1);
    private TrayIcon trayIcon = null;

    /**
     * Application stage is stored so that it can be shown and hidden based on
     * system tray icon operations.
     */
    private Stage primaryStage;
    private final List<Stage> hiddenStages = new ArrayList<>();

    public static Gui instance() {
        if (INSTANCE == null) {
            throw new NullPointerException("Gui INSTANCE is not defined yet.");
        }
        return INSTANCE;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void initTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            trayIcon = createTrayIcon(createDefaultTrayIcon());
            if (trayIcon == null) {
                LOG.log(Level.FINER, "Tray icon is null.");
                return;
            }

            MenuItem item = new MenuItem("Выйти");
            item.addActionListener((ActionEvent e) -> {
                App.exit();
            });
            trayIcon.getPopupMenu().add(item);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                LOG.log(Level.WARNING, "Can't add tray icon.", e);
                return;
            }
            /**
             * Instructs the javafx system not to exit implicitly when the last
             * application window is shut.
             */
            primaryStage.onCloseRequestProperty().set((EventHandler<WindowEvent>) (WindowEvent event) -> {
                showHide();
            });
        } else {
            LOG.log(Level.WARNING, "Tray isn't supported in this OS.");
        }
    }
    
    private void releaseTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            tray.remove(trayIcon);
        }
    }

    private TrayIcon createDefaultTrayIcon() {
        java.awt.Image image = Toolkit.getDefaultToolkit().getImage(
                ResourceManager
                        .getResourceFile("com/videoweber/lib/app/icon.png")
                        .getAbsolutePath()
        );

        PopupMenu popup = new PopupMenu();

        TrayIcon trayIcon = new TrayIcon(image, null, popup);
        trayIcon.setImageAutoSize(true);

        // One click show/hide
        trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 1) {
                    showHide();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });
        return trayIcon;
    }

    public final boolean isShowing() {
        return primaryStage.isShowing();
    }

    public final void showHide() {
        JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
            if (primaryStage.isShowing()) {
                Stage[] stages = new Stage[StageHelper.getStages().size()];
                StageHelper.getStages().toArray(stages);
                for (Stage stage : stages) {
                    if (stage.isShowing()) {
                        hiddenStages.add(stage);
                        stage.setIconified(false);
                        stage.hide();
                    }
                }
            } else {
                hiddenStages.stream().forEach((stage) -> {
                    stage.show();
                });
                hiddenStages.clear();
            }
        });
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
        INSTANCE = this;
        this.primaryStage = primaryStage;
        setUserAgentStylesheet(STYLESHEET_MODENA);
        primaryStage.getIcons().add(new Image(
                ResourceManager.getResourceFile("com/videoweber/lib/app/icon.png").toURI().toString())
        );
        /**
         * Instructs the javafx system not to exit implicitly when the last
         * application window is shut.
         */
        Platform.setImplicitExit(false);
        initTrayIcon();
        onAppStart(primaryStage);
        START_LATCH.countDown();
    }

    @Override
    public void stop() throws Exception {
        try {
            releaseTrayIcon();
            onAppStop();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in onAppStop()", e);
        }
    }

    public abstract void onAppStart(Stage primaryStage);

    public abstract void onAppStop();

    public TrayIcon createTrayIcon(TrayIcon defaultTrayIcon) {
        return defaultTrayIcon;
    }

    public abstract void onAppRun(Stage primaryStage);

    public static void runApp() {
        Platform.runLater(() -> INSTANCE.onAppRun(INSTANCE.primaryStage));
    }

    public static void waitForStar() throws InterruptedException, GuiInitTimeoutException {
        if (!START_LATCH.await(10, TimeUnit.SECONDS)) {
            throw new GuiInitTimeoutException("10 seconds elapsed.");
        }
    }

    public static Alert createAlert(Alert.AlertType alertType) {
        return createAlert(alertType, null, null, null);
    }

    public static Alert createAlert(
            Alert.AlertType alertType,
            String message
    ) {
        return createAlert(alertType, message, null, message);
    }

    public static Alert createAlert(
            Alert.AlertType alertType,
            String title,
            String contentText
    ) {
        return createAlert(alertType, title, null, contentText);
    }

    public static Alert createAlert(
            Alert.AlertType alertType,
            String title,
            String headerText,
            String contentText
    ) {
        Alert alert = new Alert(alertType);
        ((Stage) alert
                .getDialogPane()
                .getScene()
                .getWindow())
                .getIcons().add(new Image(
                        ResourceManager
                                .getResourceFile("com/videoweber/lib/app/icon.png")
                                .toURI().toString()
                ));
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert;
    }

    public static Stage createStage() {
        Stage stage = new Stage();
        stage.getIcons().add(new Image(
                ResourceManager
                        .getResourceFile("com/videoweber/lib/app/icon.png")
                        .toURI().toString()
        ));
        return stage;
    }
}
