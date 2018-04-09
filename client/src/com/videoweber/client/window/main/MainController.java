package com.videoweber.client.window.main;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.service.HibernateService;
import com.videoweber.client.widget.channel_view.ChannelViewController;
import com.videoweber.client.widget.channel_view.ChannelViewWidget;
import com.videoweber.client.window.about.AboutWindow;
import com.videoweber.client.window.download_wizard.DownloadWizardWindow;
import com.videoweber.lib.JavaFX.JavaFxSynchronous;
import com.videoweber.lib.app.App;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.widget_service.WidgetService;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.app.service.window_service.WindowService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class MainController extends WindowController {
    
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Pane widgetsPane;
    private final ArrayList<ChannelViewWidget> channelViewWidgets = new ArrayList<>();

    @Override
    public void onWindowInitialized() {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, (event) -> {
            double pixelsH = 200;
            scrollPane.setVvalue(scrollPane.getVvalue() - Math.signum(event.getDeltaY()) * pixelsH / (scrollPane.getContent().getBoundsInLocal().getHeight() - scrollPane.getViewportBounds().getHeight()));
            event.consume();
        });
        getServiceContainer()
                .getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .findAll()
                .forEach((ChannelEntity channelEntity) -> {
                    initChannelWidget(channelEntity);
                });

        getService(EventService.class).getListeners().add(
                new EntityEventListener(
                        (EntityEvent event) -> {
                            UUID deletedUuid = (UUID) event.getEntityId();
                            Iterator<ChannelViewWidget> it = channelViewWidgets.iterator();
                            while (it.hasNext()) {
                                ChannelViewWidget channelViewWidget = it.next();
                                ChannelViewController channelViewController = (ChannelViewController) channelViewWidget.getController();
                                if (channelViewController.getChannelEntity().getUuid().equals(deletedUuid)) {
                                    channelViewController.stop();
                                    JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                                        widgetsPane.getChildren().remove(channelViewWidget.getRootNode());
                                        channelViewWidgets.remove(channelViewWidget);
                                    });
                                    return;
                                }
                            }
                        },
                        ChannelEntity.class,
                        EntityOperation.DELETE,
                        null
                )
        );
        getService(EventService.class).getListeners().add(
                new EntityEventListener(
                        (EntityEvent event) -> {
                            ChannelEntity channelEntity = getServiceContainer()
                                    .getService(HibernateService.class)
                                    .getRepository(ChannelEntity.class)
                                    .get(event.getEntityId());
                            initChannelWidget(channelEntity);
                        },
                        ChannelEntity.class,
                        EntityOperation.CREATE,
                        null
                )
        );
    }

    public void openAboutAction() {
        getService(WindowService.class).openWindow(AboutWindow.class);
    }

    public void openDownloadWizardAction() {
        getService(WindowService.class).openWindow(DownloadWizardWindow.class);
    }

    private void initChannelWidget(ChannelEntity channelEntity) {
        ChannelViewWidget channelViewWidget = getService(WidgetService.class).callWidget(
                ChannelViewWidget.class,
                true,
                new Object[]{channelEntity}
        );
        channelViewWidgets.add(channelViewWidget);
        Parent rootNode = channelViewWidget.getRootNode();
        JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
            widgetsPane.getChildren().add(rootNode);
        });
    }

    public void menuExitAction() {
        App.exit();
    }

    public void release() {
        channelViewWidgets.forEach((channelViewWidget) -> {
            ((ChannelViewController) channelViewWidget.getController()).stop();
        });
    }
}
