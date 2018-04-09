package com.videoweber.lib.track.player;

import com.videoweber.lib.common.Executor;
import com.videoweber.lib.track.player.model.ReadableTrackPlayerModelInterface;
import com.videoweber.lib.track.player.model.TrackPlayerModelUpdate;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TrackPlayerFxWrap {

    private static final long MIN_WIDTH = 100;
    private static final long MIN_HEIGHT = 100;

    private final TrackPlayer player;
    private final Timeline timeline;
    private SignedBufferedImage playerImageBuffer = null;
    private BufferedImage image = null;
    private String imageSign = null;
    private final ImageView imageView = new ImageView();
    private final StackPane viewPane = new StackPane(imageView);
    private final LongProperty position = new SimpleLongProperty(this, "position");
    private final FloatProperty volume = new SimpleFloatProperty(this, "volume");
    private final ObjectProperty<ExtendedPlayerState> extendedState = new SimpleObjectProperty<>(this, "extendedState");
    private final ReadOnlyObjectWrapper<TrackPlayerState> unreachedState = new ReadOnlyObjectWrapper<>(this, "unreachedState");
    private boolean updatingFromPlayer = false;
    private final ProgressIndicator loadingIndicator = new ProgressIndicator();

    /**
     * Player would be stopped during wrapping.
     *
     * @param player
     */
    public TrackPlayerFxWrap(TrackPlayer player) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on FX application thread");
        }
        Objects.requireNonNull(player);
        this.player = player;

        position.addListener((observable, oldValue, newValue) -> {
            if (!updatingFromPlayer) {
                TrackPlayerModelUpdate modelUpdate = new TrackPlayerModelUpdate();
                modelUpdate.setPosition(newValue.longValue() * 1000);
                player.addModelUpdate(modelUpdate);
            }
        });
        volume.addListener((observable, oldValue, newValue) -> {
            if (!updatingFromPlayer) {
                TrackPlayerModelUpdate modelUpdate = new TrackPlayerModelUpdate();
                modelUpdate.setVolume(newValue.floatValue());
                player.addModelUpdate(modelUpdate);
            }
        });
        extendedState.addListener((observable, oldValue, newValue) -> {
            if (!updatingFromPlayer) {
                TrackPlayerModelUpdate modelUpdate = new TrackPlayerModelUpdate();
                modelUpdate.setState(newValue.toPlayerState());
                player.addModelUpdate(modelUpdate);
            }
        });

        Runnable modelHandler = () -> {
            updatingFromPlayer = true;
            ReadableTrackPlayerModelInterface model = player.getModelCopy();
            position.set(Math.round(model.getPosition() / 1000.));
            volume.set(model.getVolume());
            if (model.getUnreachedState() != null) {
                extendedState.set(ExtendedPlayerState.UNREACHED);
            } else {
                extendedState.set(ExtendedPlayerState.fromPlayerState(model.getState()));
            }
            unreachedState.set(model.getUnreachedState());

            // Will not update image if waiting and playing.
            if (!(model.getUnreachedState() != null && model.getUnreachedState() == TrackPlayerState.PLAYING)) {
                playerImageBuffer = player.getImageCopy(playerImageBuffer);
                BufferedImage imageFromPlayer = TrackPlayerImageTransformer.transform(playerImageBuffer, model);
                if (image != imageFromPlayer
                        || (imageFromPlayer instanceof SignedBufferedImage && !((SignedBufferedImage) imageFromPlayer).getDataSign().equals(imageSign))) {
                    image = imageFromPlayer;
                    imageView.setImage(SwingFXUtils.toFXImage(image, null));
                    if (image instanceof SignedBufferedImage) {
                        imageSign = ((SignedBufferedImage)image).getDataSign();
                    }
                }
            }

            if (model.getUnreachedState() != null) {
                loadingIndicator.setVisible(true);
            } else {
                loadingIndicator.setVisible(false);
            }

            updatingFromPlayer = false;
        };

        timeline = new Timeline(new KeyFrame(Duration.millis(250), (event) -> {
            modelHandler.run();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        player.getExecutor().getListeners().add(new Executor.Listener() {
            @Override
            public void onCrash(Executor executor) {
                Platform.runLater(() -> {
                    timeline.stop();
                    modelHandler.run();
                });
            }

            @Override
            public void onStop(Executor executor) {
                Platform.runLater(() -> {
                    timeline.stop();
                    modelHandler.run();
                });
            }

            @Override
            public void onStart(Executor executor) {
                Platform.runLater(() -> {
                    timeline.play();
                });
            }

        });

        player.getExecutor().stop();

        viewPane.setStyle("-fx-background-color: black;");
        StackPane.setAlignment(imageView, Pos.CENTER);
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(viewPane.widthProperty());
        imageView.fitHeightProperty().bind(viewPane.heightProperty());
        viewPane.setMinWidth(MIN_WIDTH);
        viewPane.setMaxWidth(Double.MAX_VALUE);
        viewPane.setMinHeight(MIN_HEIGHT);
        viewPane.setMaxHeight(Double.MAX_VALUE);

        loadingIndicator.setVisible(false);
        loadingIndicator.setMinSize(0, 0);
        loadingIndicator.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Runnable loadingIndicatorResize = () -> {
            double side = Math.min(viewPane.getWidth() / 2, viewPane.getHeight() / 2);
            loadingIndicator.setMaxSize(side, side);
        };
        viewPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            loadingIndicatorResize.run();
        });
        viewPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            loadingIndicatorResize.run();
        });

        viewPane.getChildren().add(loadingIndicator);
        StackPane.setAlignment(loadingIndicator, Pos.CENTER);

        /**
         * It needs to initialize properties because noStop will not be
         * triggered if player wasn't been running.
         */
        modelHandler.run();
    }

    public TrackPlayer getPlayer() {
        return player;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public StackPane getViewPane() {
        return viewPane;
    }

    public boolean isUpdatingFromPlayer() {
        return updatingFromPlayer;
    }

    /**
     * @return In milliseconds.
     */
    public LongProperty positionProperty() {
        return position;
    }

    public FloatProperty volumeProperty() {
        return volume;
    }

    public ObjectProperty<ExtendedPlayerState> extendedStateProperty() {
        return extendedState;
    }

    public ReadOnlyObjectProperty<TrackPlayerState> unreachedStateProperty() {
        return unreachedState.getReadOnlyProperty();
    }

}
