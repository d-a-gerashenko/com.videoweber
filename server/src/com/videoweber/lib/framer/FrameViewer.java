package com.videoweber.lib.framer;

import com.videoweber.lib.common.Executor;
import com.videoweber.lib.JavaFX.JavaFxSynchronous;
import com.videoweber.lib.common.ResourceManager;
import com.videoweber.lib.track.Size;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FrameViewer extends Executor {

    private static final ThreadLocal<SimpleDateFormat> DATE_FOMRAT_HOLDER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        }

    };
    private final FramedTrackInterface framedTrack;
    private final Pane viewContainer;
    protected final Pane decoratedViewContainer;
    protected final ImageView stopView;
    protected final ProgressIndicator loadingView;
    protected final Label timeLeftDisplay;
    private final Size size;
    private Date currentFrameDate = null;
    private long lastSuccessUpdate = -1;

    public FrameViewer(FramedTrackInterface framedTrack) {
        this(framedTrack, new Size(640, 480));
    }

    public FrameViewer(FramedTrackInterface framedTrack, Size size) {
        if (framedTrack == null || size == null) {
            throw new IllegalArgumentException();
        }

        this.framedTrack = framedTrack;
        this.size = size;

        viewContainer = new Pane();
        viewContainer.setPrefSize(size.getWidth(), size.getHeight());
        viewContainer.setMinSize(size.getWidth(), size.getHeight());
        viewContainer.setMaxSize(size.getWidth(), size.getHeight());

        stopView = new ImageView(ResourceManager.getResourceFile("com/videoweber/lib/player/logo.png").toURI().toString());
        stopView.setFitHeight(size.getHeight());
        stopView.setFitWidth(size.getWidth());
        loadingView = new ProgressIndicator();
        loadingView.setVisible(false);
        loadingView.setPrefSize(size.getWidth(), size.getHeight());
        loadingView.setMinSize(size.getWidth(), size.getHeight());
        loadingView.setMaxSize(size.getWidth(), size.getHeight());
        timeLeftDisplay = new Label();
        timeLeftDisplay.setVisible(false);
        timeLeftDisplay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        timeLeftDisplay.setTextFill(Color.web("#FFFFFF"));

        decoratedViewContainer = new Pane();
        decoratedViewContainer.setPrefSize(size.getWidth(), size.getHeight());
        decoratedViewContainer.setMinSize(size.getWidth(), size.getHeight());
        decoratedViewContainer.setMaxSize(size.getWidth(), size.getHeight());
        decoratedViewContainer.setStyle("-fx-background-color: black;");
        decoratedViewContainer.getChildren().addAll(stopView, viewContainer, loadingView, timeLeftDisplay);
    }

    public FramedTrackInterface getFramedTrackInterface() {
        return framedTrack;
    }

    public Pane getViewContainer() {
        return decoratedViewContainer;
    }

    public Size getSize() {
        return size;
    }

    private void updateTimeLeftDisplay() {
        timeLeftDisplay.setText(
                String.format(
                        "обновлено %s / снято %s",
                        getTimeLeftMessage(lastSuccessUpdate),
                        getTimeLeftMessage(currentFrameDate.getTime())
                )
        );
    }

    private static String getTimeLeftMessage(long time) {
        int minute = 60;
        int hour = 60 * minute;

        int timeLeft = (int) ((System.currentTimeMillis() - time) / 1000);

        if (timeLeft > 3 * hour) {
            return DATE_FOMRAT_HOLDER.get().format(new Date(time));
        }

        StringBuilder message = new StringBuilder();

        if (timeLeft >= hour) {
            message
                    .append(timeLeft / hour)
                    .append(" час. ");
        }
        if (timeLeft >= minute) {
            message
                    .append(timeLeft % hour / minute)
                    .append(" мин. ");
        }
        message
                .append(timeLeft % minute % hour)
                .append(" сек.")
                .append(" назад");

        return message.toString();
    }

    private void showNextFrame() {

        Frame nextFrame = framedTrack.getLastFrame();

        if (currentFrameDate != null) {
            JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                updateTimeLeftDisplay();
                timeLeftDisplay.setVisible(true);
            });
        }

        if (nextFrame == null) {
            return;
        }

        if (currentFrameDate != null && !nextFrame.getDate().after(currentFrameDate)) {
            return;
        }

        currentFrameDate = nextFrame.getDate();

        ImageView imageView = new ImageView(
                framedTrack.getLastFrame().getFile().toURI().toString()
        );
        imageView.setFitWidth(size.getWidth());
        imageView.setFitHeight(size.getHeight());
        lastSuccessUpdate = System.currentTimeMillis();

        JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
            loadingView.setVisible(false);
            updateTimeLeftDisplay();
            timeLeftDisplay.setVisible(true);
            viewContainer.getChildren().clear();
            viewContainer.getChildren().add(imageView);
        });

    }

    @Override
    public void run() {
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.FINER, "Attempt to start FrameViewer.");
            JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                loadingView.setVisible(true);
            });
            while (!isStoping()) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "FrameViewer nex step.");
                showNextFrame();
                sleep(500);
            }
        } catch (Exception e) {
            throw new RuntimeException("FrameViewer is stopped on error.", e);
        } finally {
            JavaFxSynchronous.safeRunLaterAndWaitForInfinitely(() -> {
                loadingView.setVisible(false);
                timeLeftDisplay.setText(null);
                timeLeftDisplay.setVisible(false);
                viewContainer.getChildren().clear();
            });
            lastSuccessUpdate = -1;
            currentFrameDate = null;
            Logger.getLogger(this.getClass().getName()).log(Level.FINER, "FrameViewer is stopped.");
        }
    }
}
