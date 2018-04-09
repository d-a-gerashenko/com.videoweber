package com.videoweber.server.widget.channel_record;

import com.videoweber.lib.common.Executor;
import com.videoweber.lib.gui.Gui;
import com.videoweber.lib.track.player.TrackPlayer;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.service.ChannelManagerService;
import com.videoweber.server.service.HibernateService;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityEventListener;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.server.service.track_service.ChannelTrack;
import com.videoweber.server.service.track_service.TrackService;
import com.videoweber.lib.app.service.widget_service.WidgetController;
import com.videoweber.lib.track.player.ExtendedPlayerState;
import com.videoweber.lib.track.player.TrackPlayerFxWrap;
import com.videoweber.server.service.channel_recorder_service.ChannelRecorder;
import com.videoweber.server.service.channel_recorder_service.ChannelRecorderService;
import com.videoweber.server.window.channel_archive.ChannelArchiveWindow;
import com.videoweber.server.window.channel_edit.ChannelEditWindow;
import com.videoweber.server.window.effect_list.EffectListWindow;
import com.videoweber.server.window.trigger_list.TriggerListWindow;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelRecordController extends WidgetController {

    private static final Logger LOG = Logger.getLogger(ChannelRecordController.class.getName());

    @FXML
    private Button startStopButton;
    @FXML
    private Label titleLabel;
    @FXML
    private Pane viewPane;
    @FXML
    private ToggleButton muteButton;
    @FXML
    private Circle recCircle;
    private long recCircleLastUpdate = 0;
    @FXML
    private Label restartAfterLabel;
    @FXML
    private Label inactiveChannelLabel;

    private boolean called = false;

    private static enum State {
        PLAYING, STOPPED;
    }
    private State state = State.STOPPED;
    private ChannelEntity channelEntity = null;
    private ChannelRecorder channelRecorder;
    private TrackPlayerFxWrap playerFxWrap;
    private Runnable onVolumeRise = null;
    private Timeline recorderTimeline;
    private EntityEventListener onChannelUpdate;
    private EntityEventListener onNewSample;

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

        // RECORDER
        channelRecorder = getService(ChannelRecorderService.class).getChannelRecorder(channelEntity);

        // PLAYER
        ChannelTrack track = getService(TrackService.class).createChannelTrack(channelEntity);
        TrackPlayer player = new TrackPlayer(track);
        playerFxWrap = new TrackPlayerFxWrap(player);
        playerFxWrap.positionProperty().addListener((observable, oldValue, newValue) -> {
            if (System.currentTimeMillis() - recCircleLastUpdate <= 10000) {
                return;
            }
            recCircleLastUpdate = System.currentTimeMillis();
            SampleEntity sampleEntity = getService(TrackService.class).getSample(channelEntity, new Date(newValue.longValue()));
            if (sampleEntity != null
                    && sampleEntity.isRecorded()
                    && state == State.PLAYING) {
                recCircle.setVisible(true);
            } else {
                recCircle.setVisible(false);
            }
        });
        viewPane.getChildren().add(playerFxWrap.getViewPane());
        player.getExecutor().getListeners().add(new Executor.Listener() {
            @Override
            public void onCrash(Executor executor) {
                LOG.log(Level.SEVERE, "Player crashed.", executor.getLastCrashException());
            }

        });
        player.getExecutor().start();
        playerFxWrap.volumeProperty().set(0);
        recorderTimeline = new Timeline(new KeyFrame(Duration.millis(500), (event) -> {
            if (channelRecorder.isActive()) {
                inactiveChannelLabel.setVisible(false);
            } else {
                inactiveChannelLabel.setVisible(true);
            }
            long timeToRestart = channelRecorder.getTimeToRestart();
            if (timeToRestart > 0) {
                restartAfterLabel.setVisible(true);
                restartAfterLabel.setText(String.format("Произошла ошибка, канал будет перезапущен через %s сек.", timeToRestart));
            } else {
                restartAfterLabel.setVisible(false);
            }
        }));
        recorderTimeline.setCycleCount(Animation.INDEFINITE);
        recorderTimeline.play();

        // GUI
        restartAfterLabel.setVisible(false);
        inactiveChannelLabel.setVisible(false);
        recCircle.setVisible(false);
        muteButton.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                playerFxWrap.volumeProperty().setValue(1);
            } else {
                playerFxWrap.volumeProperty().setValue(0);
            }
        });
        muteButton.setSelected(false);
        Runnable setChannelHeader = () -> {
            getService(HibernateService.class).refresh(channelEntity);
            titleLabel.setText(channelEntity.getTitle() + "\n" + channelEntity.getUuid());
        };
        setChannelHeader.run();
        onChannelUpdate = new EntityEventListener(
                (EntityEvent event) -> {
                    Platform.runLater(() -> {
                        setChannelHeader.run();
                    });
                },
                ChannelEntity.class, EntityOperation.UPDATE, channelEntity.getUuid()
        );
        getService(EventService.class).getListeners().add(onChannelUpdate);
        onNewSample = new EntityEventListener(
                (EntityEvent event) -> {
                    Platform.runLater(() -> {
                        if (getService(HibernateService.class)
                                .getRepository(SampleEntity.class)
                                .get(event.getEntityId())
                                .getChannel()
                                .getUuid().equals(channelEntity.getUuid())) {
                            if (playerFxWrap.unreachedStateProperty().getValue() != null) {
                                playerFxWrap.extendedStateProperty().setValue(ExtendedPlayerState.PLAYING);
                            }
                        }
                    });
                },
                SampleEntity.class, EntityOperation.CREATE, null
        );
        getService(EventService.class).getListeners().add(onNewSample);
    }

    public void start() {
        if (state == State.PLAYING) {
            return;
        }
        state = State.PLAYING;
        startStopButton.setText("Стоп");
        playerFxWrap.positionProperty().set(System.currentTimeMillis());
        playerFxWrap.extendedStateProperty().set(ExtendedPlayerState.PLAYING);
        channelRecorder.start();
    }

    public void stop() {
        if (state == State.STOPPED) {
            return;
        }
        state = State.STOPPED;
        startStopButton.setText("Старт");
        playerFxWrap.extendedStateProperty().set(ExtendedPlayerState.STOPPED);
        channelRecorder.stop();
    }
    
    public void release() {
        playerFxWrap.getPlayer().getExecutor().stopAndWaitForInfinitely();
        channelRecorder.stop();
    }

    public void mute() {
        if (muteButton.isSelected()) {
            muteButton.fire();
        }
    }

    public synchronized void muteButtonAction() {
        if (muteButton.isSelected()) {
            if (onVolumeRise != null) {
                onVolumeRise.run();
            }
        }
    }

    public synchronized void startStopButtonAction() {
        if (state == State.PLAYING) {
            stop();
        } else {
            start();
        }
    }

    public Runnable getOnVolumeRise() {
        return onVolumeRise;
    }

    public void setOnVolumeRise(Runnable onVolumeRise) {
        this.onVolumeRise = onVolumeRise;
    }

    public ChannelEntity getChannelEntity() {
        return channelEntity;
    }

    public void deleteChannelAction() {
        if (Gui.createAlert(
                Alert.AlertType.CONFIRMATION,
                "Удаление канала",
                "Вместе с каналом будут безвозвратно удалены все его записи.",
                String.format(
                        "Вы действительно хотите удалить канал \"%s\" с идентификатором \"%s\"?",
                        channelEntity.getTitle(),
                        channelEntity.getUuid()
                )
        ).showAndWait().get() != ButtonType.OK) {
            return;
        }
        getService(EventService.class).getListeners().remove(onChannelUpdate);
        getService(EventService.class).getListeners().remove(onNewSample);
        stop();
        recorderTimeline.stop();
        playerFxWrap.getPlayer().getExecutor().stop();

        getService(ChannelManagerService.class).delete(channelEntity);

        getService(EventService.class).trigger(
                new EntityEvent(
                        ChannelEntity.class,
                        EntityOperation.DELETE,
                        channelEntity.getUuid()
                )
        );
    }

    public void editChannelAction() {
        getService(WindowService.class)
                .openWindow(
                        ChannelEditWindow.class,
                        new Object[]{channelEntity.getUuid()}
                );
    }

    public void editEffectsAction() {
        getService(WindowService.class).openWindow(EffectListWindow.class,
                new Object[]{channelEntity.getUuid()}
        );
    }

    public void editTriggersAction() {
        getService(WindowService.class).openWindow(TriggerListWindow.class,
                new Object[]{channelEntity.getUuid()}
        );
    }

    public void openArchiveAction() {
        getService(WindowService.class).openWindow(ChannelArchiveWindow.class,
                new Object[]{channelEntity.getUuid()}
        );
    }

}
