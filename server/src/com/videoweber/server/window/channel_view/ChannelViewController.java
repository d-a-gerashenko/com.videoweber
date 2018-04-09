package com.videoweber.server.window.channel_view;

import com.videoweber.lib.JavaFX.JavaFxPlatform;
import com.videoweber.lib.channel.Channel;
import com.videoweber.lib.common.Executor;
import com.videoweber.lib.engines.javacv.FfmpegEffectProcessor;
import com.videoweber.lib.engines.javacv.FfmpegProbeFactory;
import com.videoweber.lib.recorder.Recorder;
import com.videoweber.lib.recorder.Trigger;
import com.videoweber.lib.sampler.Effect;
import com.videoweber.lib.sampler.EffectProcessor;
import com.videoweber.lib.sampler.ProbeFactory;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.sampler.SampleFactory;
import com.videoweber.lib.sampler.Sampler;
import com.videoweber.lib.sampler.SamplerEngine;
import com.videoweber.lib.track.TempTrack;
import com.videoweber.lib.track.Track;
import com.videoweber.lib.app.service.TempDirService;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.engines.javacv.FfmpegSamplerEngine;
import com.videoweber.lib.gui.Gui;
import com.videoweber.lib.track.player.ExtendedPlayerState;
import com.videoweber.lib.track.player.TrackPlayer;
import com.videoweber.lib.track.player.TrackPlayerFxWrap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelViewController extends WindowController {

    private static enum State {
        PLAYING, STOPPED;
    }

    @FXML
    Parent root;
    @FXML
    private Button startStopButton;
    @FXML
    private Pane viewPane;
    @FXML
    private ToggleButton soundButton;
    @FXML
    private Circle recCircle;

    private State state = null;
    private Sampler sampler = null;
    private TrackPlayerFxWrap trackPlayerFxWrap = null;

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void onWindowInitialized() {
        getStage().setOnCloseRequest((WindowEvent event) -> {
            if (trackPlayerFxWrap != null) {
                trackPlayerFxWrap.getPlayer().getExecutor().stopAndWaitForInfinitely();
                viewPane.getChildren().clear();
                trackPlayerFxWrap = null;
            }
            if (sampler != null) {
                sampler.stopAndWaitForInfinitely();
                sampler = null;
            }
        });
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        if (parameters == null || !(parameters[0] instanceof Channel)) {
            throw new IllegalArgumentException();
        }
        state = State.STOPPED;
        Channel channel = (Channel) parameters[0];

        Effect[] effects = null;
        Trigger[] triggers = null;

        if (parameters.length == 2) {
            if (parameters[1] instanceof HashMap) {
                HashMap options = (HashMap) parameters[1];

                if (options.get("effects") != null) {
                    if (options.get("effects") instanceof Effect[]) {
                        effects = (Effect[]) options.get("effects");
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                if (options.get("triggers") != null) {
                    if (options.get("triggers") instanceof Trigger[]) {
                        triggers = (Trigger[]) options.get("triggers");
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (effects == null) {
            effects = new Effect[0];
        }
        if (triggers == null) {
            triggers = new Trigger[0];
        }

        SamplerEngine samplerEngine = new FfmpegSamplerEngine(
                channel,
                getService(TempDirService.class).createTempDir()
        );
        ProbeFactory probeFactory = new FfmpegProbeFactory();
        SampleFactory sampleFactory = new SampleFactory(probeFactory);
        sampler = new Sampler(samplerEngine, sampleFactory);

        EffectProcessor effectProcessor = new FfmpegEffectProcessor(
                getService(TempDirService.class).createTempDir()
        );
        List<Effect> effectsList = Arrays.asList(effects);
        effectProcessor.getEffects().addAll(effectsList);
        sampler.setSampleHandler(effectProcessor);

        Recorder recorder = new Recorder();
        recorder.getTriggers().addAll(Arrays.asList(triggers));

        TempTrack tempTrak = new TempTrack();
        effectProcessor.setSampleHandler((Sample sample) -> {
            trackPlayerFxWrap.extendedStateProperty().set(ExtendedPlayerState.PLAYING);
            tempTrak.onSample(sample);
            recorder.onSample(sample);
        });

        ArrayList<Sample> lastRecoredSamples = new ArrayList<>();
        recorder.setRecordHandler((Sample sample) -> {
            while (lastRecoredSamples.size() >= tempTrak.getSamplesCountLimit()) {
                lastRecoredSamples.remove(0);
            }
            lastRecoredSamples.add(sample);
        });
        Track track = tempTrak;
        trackPlayerFxWrap = new TrackPlayerFxWrap(new TrackPlayer(track));
        trackPlayerFxWrap.getPlayer().getExecutor().start();
        trackPlayerFxWrap.positionProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            Sample sample = tempTrak.getSample(new Date(newValue.longValue()));
            if (sample != null && lastRecoredSamples.contains(sample)) {
                JavaFxPlatform.safeRunLater(() -> {
                    recCircle.setVisible(true);
                });
            } else {
                JavaFxPlatform.safeRunLater(() -> {
                    recCircle.setVisible(false);
                });
            }
        });
        recCircle.setVisible(false);

        soundButton.setSelected(true);
        trackPlayerFxWrap.volumeProperty().set(1);

        viewPane.getChildren().add(trackPlayerFxWrap.getViewPane());
        startStopButton.setText("Запустить");

        sampler.getListeners().add(new Executor.Listener() {
            @Override
            public void onCrash(Executor executor) {
                JavaFxPlatform.safeRunLater(() -> {
                    Gui.createAlert(Alert.AlertType.WARNING, "Не удается воспроизвести канал.");
                    state = State.STOPPED;
                    startStopButton.setText("Остановить");
                    trackPlayerFxWrap.extendedStateProperty().set(ExtendedPlayerState.STOPPED);
                });
            }
        });
    }

    public void soundButtonAction() {
        if (soundButton.isSelected()) {
            trackPlayerFxWrap.volumeProperty().set(1);
        } else {
            trackPlayerFxWrap.volumeProperty().set(0);
        }
    }

    public void startStopButtonAction() {
        if (state == State.PLAYING) {
            startStopButton.setText("Запустить");
            state = State.STOPPED;
            sampler.stopAndWaitForInfinitely();
            trackPlayerFxWrap.extendedStateProperty().setValue(ExtendedPlayerState.STOPPED);
        } else {
            startStopButton.setText("Остановить");
            state = State.PLAYING;
            sampler.start();
            trackPlayerFxWrap.extendedStateProperty().setValue(ExtendedPlayerState.PLAYING);
            recCircle.setVisible(false);
        }
    }
}
