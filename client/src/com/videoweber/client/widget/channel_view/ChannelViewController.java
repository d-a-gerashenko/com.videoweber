package com.videoweber.client.widget.channel_view;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.service.HibernateService;
import com.videoweber.client.service.communicator_service.ChannelOnlineStatusChangedEvent;
import com.videoweber.client.service.communicator_service.CommunicatorService;
import com.videoweber.client.service.framed_channel_track_service.FramedChannelTrack;
import com.videoweber.client.service.framed_channel_track_service.FramedChannelTrackService;
import com.videoweber.client.window.channel_archive.ChannelArchiveWindow;
import com.videoweber.lib.JavaFX.JavaFxSynchronous;
import com.videoweber.lib.app.service.event_service.EventListener;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.SpecifiedEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.widget_service.WidgetController;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.framer.FrameViewer;
import com.videoweber.lib.track.Size;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelViewController extends WidgetController {

    @FXML
    private Label titleLabel;
    @FXML
    private Pane viewPane;

    private boolean called = false;
    private ChannelEntity channelEntity = null;
    private FrameViewer frameViewer = null;
    private final ArrayList<EventListener> registredEvents = new ArrayList<>();

    @Override
    public void onWidgetCall(Object[] parameters) {
        if (called) {
            throw new IllegalStateException("Can't call this widget instance twice.");
        } else {
            called = true;
        }
        if (parameters == null || !(parameters[0] instanceof ChannelEntity)) {
            throw new IllegalArgumentException();
        }

        channelEntity = (ChannelEntity) parameters[0];

        refresh();
    }

    public ChannelEntity getChannelEntity() {
        return channelEntity;
    }

    public void start() {
        if (frameViewer != null) {
            frameViewer.start();
        }
    }

    public void stop() {
        if (frameViewer != null) {
            frameViewer.stop();
        }
    }

    private void refresh() {
        getService(HibernateService.class).refresh(channelEntity);

        stop();
        unregisterAllListeners();
        titleLabel.setText("---");
        frameViewer = null;
        viewPane.getChildren().clear();

        registerListener(new EntityEventListener(
                (EntityEvent event) -> {
                    JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                        refresh();
                    });
                },
                ChannelEntity.class,
                EntityOperation.UPDATE,
                channelEntity.getUuid()
        ));

        titleLabel.setText(channelEntity.getTitle() + "\n" + channelEntity.getUuid());
        if (getService(CommunicatorService.class).getOnlineChannels().contains(channelEntity.getUuid())) {
            titleLabel.setTextFill(Color.web("#80EE36"));
        } else {
            titleLabel.setTextFill(Color.web("#EEA236"));
        }
        registerListener(new SpecifiedEventListener<>(
                ChannelOnlineStatusChangedEvent.class,
                (ChannelOnlineStatusChangedEvent event) -> {
                    if (!channelEntity.getUuid().equals(event.getChannel())) {
                        return;
                    }
                    if (event.getStatus() == ChannelOnlineStatusChangedEvent.Status.ONLINE) {
                        JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                            titleLabel.setTextFill(Color.web("#80EE36"));
                        });
                    } else {
                        JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                            titleLabel.setTextFill(Color.web("#EEA236"));
                        });
                    }
                }
        ));

        FramedChannelTrack framedChannelTrack = getService(FramedChannelTrackService.class).getFramedChannelTrack(channelEntity);
        frameViewer = new FrameViewer(framedChannelTrack, new Size(560, 360));
        viewPane.getChildren().add(frameViewer.getViewContainer());
        start();
    }

    private void registerListener(EventListener eventListener) {
        registredEvents.add(eventListener);
        getService(EventService.class).getListeners().add(eventListener);
    }

    private void unregisterAllListeners() {
        EventService eventService = getService(EventService.class);
        eventService.getListeners().removeAll(registredEvents);
        registredEvents.clear();
    }

    public void openArchiveAction() {
        getService(WindowService.class).openWindow(ChannelArchiveWindow.class,
                new Object[]{channelEntity.getUuid()}
        );
    }

}
