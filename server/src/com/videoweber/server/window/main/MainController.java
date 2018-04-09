package com.videoweber.server.window.main;

import com.videoweber.lib.app.App;
import com.videoweber.lib.app.service.ParametersService;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.widget_service.WidgetService;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.server.widget.channel_record.ChannelRecordController;
import com.videoweber.server.widget.channel_record.ChannelRecordWidget;
import com.videoweber.server.window.about.AboutWindow;
import com.videoweber.server.window.source_list.SourceListWindow;
import com.videoweber.server.window.storage_list.StorageListWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class MainController extends WindowController {

    @FXML
    private Pane widgetsPane;

    @FXML
    private ScrollPane scrollPane;

    private final ArrayList<ChannelRecordWidget> channelRecordWidgets = new ArrayList<>();

    private final DataFormat SERIALIZED_MIME_TYPE = new DataFormat(String.valueOf(this.hashCode()));

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
        if (getService(ParametersService.class)
                .getProperty("START_CHANNELS_AT_STARTUP")
                .equals("1")) {
            startAllChannelsAction();
        }
        getService(EventService.class).getListeners().add(
                new EntityEventListener(
                        (EntityEvent event) -> {
                            UUID deletedUuid = (UUID) event.getEntityId();
                            Iterator<ChannelRecordWidget> it = channelRecordWidgets.iterator();
                            while (it.hasNext()) {
                                ChannelRecordWidget channelRecordWidget = it.next();
                                ChannelRecordController channelRecordController = (ChannelRecordController) channelRecordWidget.getController();
                                if (channelRecordController.getChannelEntity().getUuid().equals(deletedUuid)) {
                                    widgetsPane.getChildren().remove(channelRecordWidget.getRootNode());
                                    channelRecordWidgets.remove(channelRecordWidget);
                                    return;
                                }
                            }
                        },
                        ChannelEntity.class,
                        EntityOperation.DELETE,
                        null
                )
        );
    }

    private void initChannelWidget(ChannelEntity channelEntity) {
        initChannelWidget(channelEntity, true);
    }

    private void initChannelWidget(ChannelEntity channelEntity, boolean toTail) {
        ChannelRecordWidget channelRecordWidget = getService(WidgetService.class).callWidget(
                ChannelRecordWidget.class,
                true,
                new Object[]{channelEntity}
        );
        ((ChannelRecordController) channelRecordWidget.getController())
                .setOnVolumeRise(() -> {
                    channelRecordWidgets.stream().filter((ChannelRecordWidget crw) -> {
                        return crw != channelRecordWidget;
                    }).forEach((ChannelRecordWidget crw1) -> {
                        ((ChannelRecordController) crw1.getController()).mute();
                    });
                });
        channelRecordWidgets.add(channelRecordWidget);
        Parent rootNode = channelRecordWidget.getRootNode();
        if (toTail) {
            widgetsPane.getChildren().add(rootNode);
        } else {
            widgetsPane.getChildren().add(0, rootNode);
        }
        rootNode.setOnDragDetected(event -> {
            Integer draggedIndex = widgetsPane.getChildren().indexOf(rootNode);
            Dragboard db = rootNode.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(rootNode.snapshot(null, null));
            ClipboardContent cc = new ClipboardContent();
            cc.put(SERIALIZED_MIME_TYPE, draggedIndex);
            db.setContent(cc);
            event.consume();
        });
        rootNode.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                if (widgetsPane.getChildren().indexOf(rootNode) != ((Integer) db.getContent(SERIALIZED_MIME_TYPE))) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    event.consume();
                }
            }
        });
        rootNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                int dropIndex;
                dropIndex = widgetsPane.getChildren().indexOf(rootNode);

                int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                Node draggedItem = widgetsPane.getChildren().get(draggedIndex);
                widgetsPane.getChildren().remove(draggedItem);

                widgetsPane.getChildren().add(dropIndex, draggedItem);

                event.setDropCompleted(true);
                event.consume();
                onChannelsReordered();
            }
        });
    }

    public void menuExitAction() {
        App.exit();
    }

    public void startAllChannelsAction() {
        channelRecordWidgets.forEach((ChannelRecordWidget channelRecordWidget) -> {
            ((ChannelRecordController) channelRecordWidget.getController()).start();
        });
    }

    public void stopAllChannelsAction() {
        channelRecordWidgets.forEach((ChannelRecordWidget channelRecordWidget) -> {
            ((ChannelRecordController) channelRecordWidget.getController()).stop();
        });
    }
    
    public void release() {
        channelRecordWidgets.forEach((ChannelRecordWidget channelRecordWidget) -> {
            ((ChannelRecordController) channelRecordWidget.getController()).release();
        });
    }

    public void openStorageListAction() {
        getService(WindowService.class).openWindow(StorageListWindow.class);
    }

    public void openSourceListAction() {
        getService(WindowService.class).openWindow(SourceListWindow.class);
    }

    public void openAboutAction() {
        getService(WindowService.class).openWindow(AboutWindow.class);
    }

    public void onChannelsReordered() {
        ChannelRepository repository = (ChannelRepository) getService(HibernateService.class)
                .getRepository(ChannelEntity.class);
        HashMap<UUID, ChannelEntity> entities = new HashMap<>();
        repository.findAll().forEach((ChannelEntity entity) -> {
            entities.put(entity.getUuid(), entity);
        });

        ListIterator<ChannelRecordWidget> iterator = channelRecordWidgets.listIterator();
        while (iterator.hasNext()) {
            ChannelRecordWidget channelRecordWidget = iterator.next();
            ChannelRecordController channelRecordController = (ChannelRecordController) channelRecordWidget.getController();
            entities.get(channelRecordController.getChannelEntity().getUuid())
                    .setOrder(widgetsPane.getChildren().indexOf(
                            channelRecordWidget.getRootNode()
                    ));
        }

        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            entities.values().forEach((ChannelEntity entity) -> {
                session.merge(entity);
            });
            session.flush();
        });
    }

    public void newChannelAction() {
        ChannelEntity newChannelEntity = new ChannelEntity();
        newChannelEntity.setTitle("Новый канал");
        newChannelEntity.setOrder(0);
        List<ChannelEntity> entities = getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .findAll();
        entities.forEach((ChannelEntity channelEntity) -> {
            channelEntity.setOrder(channelEntity.getOrder() + 1);
        });
        getServiceContainer().getService(HibernateService.class).acquireSession((session) -> {
            session.persist(newChannelEntity);
            entities.forEach((ChannelEntity channelEntity) -> {
                session.merge(channelEntity);
            });
            session.flush();
        });
        initChannelWidget(newChannelEntity, false);

        getService(EventService.class).trigger(
                new EntityEvent(
                        ChannelEntity.class,
                        EntityOperation.CREATE,
                        newChannelEntity.getUuid()
                )
        );
    }

}
