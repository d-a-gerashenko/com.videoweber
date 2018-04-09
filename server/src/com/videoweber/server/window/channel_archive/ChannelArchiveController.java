package com.videoweber.server.window.channel_archive;

import com.videoweber.lib.JavaFX.DateTimeSlider;
import com.videoweber.lib.JavaFX.DateTimeSelector;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.service.HibernateService;
import com.videoweber.server.service.track_service.ChannelTrack;
import com.videoweber.server.service.track_service.TrackService;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.common.Executor;
import com.videoweber.lib.track.player.ExtendedPlayerState;
import com.videoweber.lib.track.player.TrackPlayer;
import com.videoweber.lib.track.player.TrackPlayerFxWrap;
import com.videoweber.lib.track.player.TrackPlayerState;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelArchiveController extends WindowController {

    private static final Logger LOG = Logger.getLogger(ChannelArchiveController.class.getName());

    @FXML
    private Parent root;
    @FXML
    private Pane dateTimeSelectorPane;
    @FXML
    private Pane scaleSelectorPane;
    @FXML
    private AnchorPane dateTimeSliderPane;
    @FXML
    private StackPane viewPane;
    @FXML
    private Button startStopButton;
    @FXML
    private ToggleButton soundButton;

    private ChannelEntity detachedChannelEntity;
    private TrackPlayerFxWrap trackPlayerFxWrap = null;
    private final DateTimeSlider dateTimeSlider = new DateTimeSlider();
    private final DateTimeSelector dateTimeSelector = new DateTimeSelector();
    private final ScaleSelector scaleSelector = new ScaleSelector();

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void onWindowInitialized() {
        dateTimeSliderPane.getChildren().add(dateTimeSlider);
        AnchorPane.setTopAnchor(dateTimeSlider, 0.);
        AnchorPane.setRightAnchor(dateTimeSlider, 0.);
        AnchorPane.setBottomAnchor(dateTimeSlider, 0.);
        AnchorPane.setLeftAnchor(dateTimeSlider, 0.);
        dateTimeSlider.setOnRullerUpdate(new Consumer<Canvas>() {
            private Long lastMin = null;
            private Long lastMax = null;
            private final Canvas lastCanvas = new Canvas();
            private long lastFullUpdate = System.currentTimeMillis();
            private UUID channelEntityUid = null;

            @Override
            public void accept(Canvas ruller) {
                if (detachedChannelEntity != null) {
                    long scale = dateTimeSlider.getMax() - dateTimeSlider.getMin();
                    double pixelsPerMillis = ruller.getWidth() / scale;

                    Long cachedMin = null;
                    Long cachedMax = null;
                    Long renderingMin = dateTimeSlider.getMin();
                    Long renderingMax = dateTimeSlider.getMax();

                    if (lastMin != null
                            && System.currentTimeMillis() - lastFullUpdate < 5000
                            && Objects.equals(channelEntityUid, detachedChannelEntity.getUuid())) {
                        double lastPixelsPerMillis = lastCanvas.getWidth() / (lastMax - lastMin);
                        if (pixelsPerMillis == lastPixelsPerMillis) {
                            if (dateTimeSlider.getMin() == lastMin && dateTimeSlider.getMax() == lastMax) {
                                cachedMin = dateTimeSlider.getMin();
                                cachedMax = dateTimeSlider.getMax();
                                renderingMin = null;
                                renderingMax = null;
                            } else if (dateTimeSlider.getMin() > lastMin && dateTimeSlider.getMin() < lastMax) {
                                cachedMin = dateTimeSlider.getMin();
                                cachedMax = lastMax;
                                renderingMin = lastMax;
                                renderingMax = dateTimeSlider.getMax();
                            } else if (dateTimeSlider.getMax() > lastMin && dateTimeSlider.getMax() < lastMax) {
                                cachedMin = lastMin;
                                cachedMax = dateTimeSlider.getMax();
                                renderingMin = dateTimeSlider.getMin();
                                renderingMax = lastMin;
                            }
                        } else {
                            channelEntityUid = detachedChannelEntity.getUuid();
                            lastFullUpdate = System.currentTimeMillis();
                        }
                    }

                    SnapshotParameters snapshotParams = new SnapshotParameters();
                    snapshotParams.setFill(Color.TRANSPARENT);
                    WritableImage imageBuffer = lastCanvas.snapshot(snapshotParams, null);
                    lastCanvas.setWidth(ruller.getWidth());
                    lastCanvas.setHeight(ruller.getHeight());
                    lastCanvas.getGraphicsContext2D().clearRect(0, 0, ruller.getWidth(), ruller.getHeight());

                    if (renderingMin != null) {
                        long renderingShiftDuration = 0;
                        if (Objects.equals(renderingMin, lastMax)) {
                            renderingShiftDuration = lastMax - dateTimeSlider.getMin();
                        }

                        final long renderingMinFinal = renderingMin;
                        final long renderingShiftDurationFinal = renderingShiftDuration;

                        SampleRepository repository = (SampleRepository) getService(HibernateService.class).getRepository(SampleEntity.class);
                        repository.findByRange(
                                detachedChannelEntity,
                                new Date(renderingMin),
                                new Date(renderingMax)
                        ).forEach((SampleEntity se) -> {
                            if (se.isRecorded()) {
                                lastCanvas.getGraphicsContext2D().setFill(Color.web("#FF0000"));
                            } else {
                                lastCanvas.getGraphicsContext2D().setFill(Color.web("#999999"));
                            }

                            double x = (se.getBegin() - renderingMinFinal + renderingShiftDurationFinal) * pixelsPerMillis;
                            double y = 0;
                            double w = se.getDuration() * pixelsPerMillis;
                            double h = 2;
                            lastCanvas.getGraphicsContext2D().fillRect(x, y, w, h);
                        });
                    }

                    if (cachedMin != null) {
                        long cachedShiftDuration = 0;
                        if (Objects.equals(cachedMin, lastMin)) {
                            cachedShiftDuration = lastMin - dateTimeSlider.getMin();
                        }
                        long cacheDuration = cachedMax - cachedMin;
                        double cacheWidth = Math.round(cacheDuration * pixelsPerMillis);
                        long cacheOffsetDuration = cachedMin - lastMin;
                        lastCanvas.getGraphicsContext2D().drawImage(
                                imageBuffer,
                                Math.round(cacheOffsetDuration * pixelsPerMillis), // sx
                                0, // sy
                                cacheWidth, // sw
                                lastCanvas.getHeight(), // sh
                                Math.round(cachedShiftDuration * pixelsPerMillis), // dx
                                0, // dy
                                cacheWidth, // dw
                                lastCanvas.getHeight() // dh
                        );
                    }

                    ruller.getGraphicsContext2D().drawImage(lastCanvas.snapshot(snapshotParams, null), 0, 0);

                    lastMin = dateTimeSlider.getMin();
                    lastMax = dateTimeSlider.getMax();
                }
            }
        });

        dateTimeSelectorPane.getChildren().add(dateTimeSelector);

        dateTimeSelector.valueProperty().bindBidirectional(dateTimeSlider.positionProperty());

        scaleSelectorPane.getChildren().add(scaleSelector);
        scaleSelector.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            dateTimeSlider.setScale(scaleSelector.getItems().get(newValue.intValue()));
        });

        getStage().setOnCloseRequest((WindowEvent event) -> {
            if (trackPlayerFxWrap != null) {
                trackPlayerFxWrap.getPlayer().getExecutor().stopAndWaitForInfinitely();
                viewPane.getChildren().clear();
                trackPlayerFxWrap = null;
            }
        });
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        if (parameters == null
                || parameters.length == 0
                || !(parameters[0] instanceof UUID)) {
            throw new IllegalArgumentException();
        }
        UUID entityId = (UUID) parameters[0];
        detachedChannelEntity = getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .get(entityId);
        if (detachedChannelEntity == null) {
            throw new NullPointerException();
        }

        getStage().setTitle(String.format(
                "Просмотр архива канала \"%s\"",
                detachedChannelEntity.getTitle()
        ));

        ChannelTrack channelTrack = getService(TrackService.class).createChannelTrack(detachedChannelEntity);

        TrackPlayer player = new TrackPlayer(channelTrack);
        player.getExecutor().getListeners().add(new Executor.Listener() {
            @Override
            public void onCrash(Executor executor) {
                LOG.log(Level.WARNING, "Player crashed.", executor.getLastCrashException());
            }

        });
        trackPlayerFxWrap = new TrackPlayerFxWrap(player);
        player.getExecutor().start();
        // Disabling waiting. Stop on no samples.
        trackPlayerFxWrap.unreachedStateProperty().addListener((ObservableValue<? extends TrackPlayerState> observable, TrackPlayerState oldValue, TrackPlayerState newValue) -> {
            if (newValue != null) {
                // This updating is from player. Scheduling change to push it in player.
                Platform.runLater(() -> {
                    trackPlayerFxWrap.extendedStateProperty().set(ExtendedPlayerState.STOPPED);
                });
            }
        });
        
        scaleSelector.getSelectionModel().select(DateTimeSlider.Scale.HOUR);
        dateTimeSlider.positionProperty().bindBidirectional(trackPlayerFxWrap.positionProperty());
        trackPlayerFxWrap.positionProperty().set(System.currentTimeMillis());
        
        trackPlayerFxWrap.extendedStateProperty().addListener((ObservableValue<? extends ExtendedPlayerState> observable, ExtendedPlayerState oldValue, ExtendedPlayerState newValue) -> {
            if (newValue.toPlayerState() == TrackPlayerState.PLAYING) {
                startStopButton.setText("Остановить");
                startStopButton.setOnAction((ActionEvent ae) -> {
                    trackPlayerFxWrap.extendedStateProperty().set(ExtendedPlayerState.PAUSED);
                });
            } else if (newValue.toPlayerState() != TrackPlayerState.PLAYING) {
                startStopButton.setText("Запустить");
                startStopButton.setOnAction((ActionEvent ae) -> {
                    trackPlayerFxWrap.extendedStateProperty().set(ExtendedPlayerState.PLAYING);
                });
            }
        });
        trackPlayerFxWrap.volumeProperty().set(1);
        soundButton.setSelected(true);
        viewPane.getChildren().add(trackPlayerFxWrap.getViewPane());

        startStopButton.setText("Запустить");
        startStopButton.setOnAction((ActionEvent startEvent) -> {
            trackPlayerFxWrap.extendedStateProperty().set(ExtendedPlayerState.PLAYING);
        });

        SampleRepository sampleRepository = (SampleRepository) getService(HibernateService.class)
                .getRepository(SampleEntity.class);
        final Callback<DatePicker, DateCell> dayCellFactory
                = (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                Date day = Date.from(item.atStartOfDay(ZoneId.systemDefault()).toInstant());

                if (sampleRepository.getCountByDay(detachedChannelEntity, day) > 0) {
                    setStyle("-fx-background-color: #FF0000;");
                }
            }
        };
        dateTimeSelector.getDate().setDayCellFactory(dayCellFactory);
    }

    public void soundButtonAction() {
        if (soundButton.isSelected()) {
            trackPlayerFxWrap.volumeProperty().setValue(1);
        } else {
            trackPlayerFxWrap.volumeProperty().setValue(0);
        }
    }

    public void resetViewportAction() {
        dateTimeSlider.resetViewport();
    }
}
