package com.videoweber.lib.JavaFX;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;

/**
 *
 * @author gda
 */
public class DateTimeSlider extends AnchorPane {

    public static enum Scale {
        HOUR_24(86400),
        HOUR_8(28800),
        HOUR_3(10800),
        HOUR_2(7200),
        HOUR(3600),
        MINUTE_30(1800),
        MINUTE_10(600),
        MINUTE(60),
        SECOND_30(30);

        private final long seconds;

        private Scale(long seconds) {
            this.seconds = seconds;
        }

        public long getSeconds() {
            return seconds;
        }

    }
    private final Viewport viewport;
    private final int sliderPaddingPercent = 10;
    private Consumer<Canvas> onRullerUpdate = null;

    /**
     * In seconds.
     */
    private final LongProperty scaleProperty = new LongPropertyBase() {

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "scale";
        }

    };

    /**
     * In milliseconds.
     */
    private final LongProperty positionProperty = new LongPropertyBase() {

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "position";
        }

    };

    private final Canvas ruller = new Canvas();

    public DateTimeSlider() {
        VBox vBox = new VBox();
        getChildren().add(vBox);
        AnchorPane.setTopAnchor(vBox, 0.);
        AnchorPane.setRightAnchor(vBox, 0.);
        AnchorPane.setBottomAnchor(vBox, 0.);
        AnchorPane.setLeftAnchor(vBox, 0.);

        viewport = new Viewport();
        viewport.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(object.longValue());
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                int millisecond = calendar.get(Calendar.MILLISECOND);
                return String.format(
                        "%02d.%02d.%s \n"
                        + "%02d:%02d:%02d.%03d",
                        day, month, year,
                        hour, minute, second, millisecond
                );
            }

            @Override
            public Double fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        viewport.setMinorTickCount(0);
        viewport.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            positionProperty.setValue(newValue);
        });
        vBox.getChildren().add(viewport);

        Pane rullerPane = new Pane(ruller);
        vBox.getChildren().add(rullerPane);

        scaleProperty.set(Scale.HOUR.getSeconds());
        scaleProperty.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            // Sets slider min and max. Slider value doesn't changed.
            double sliderPositionInPercents = 100 * (viewport.getValue() - viewport.getMin()) / (viewport.getMax() - viewport.getMin());
            long scaleInMilliseconds = newValue.longValue() * 1000;
            viewport.setMin(viewport.getValue() - sliderPositionInPercents * scaleInMilliseconds / 100);
            viewport.setMax(viewport.getMin() + scaleInMilliseconds);
            updateRuler();
        });

        positionProperty.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            /**
             * Changes slider value. Changes slider`s min and max values if it's
             * value is out of range.
             */
            long currentPosition = newValue.longValue();
            long scaleInMilliseconds = scaleProperty.longValue() * 1000;
            long sliderPaddingInMilliseconds = sliderPaddingPercent * scaleInMilliseconds / 100;
            if (currentPosition < viewport.getMin() + sliderPaddingInMilliseconds) {
                viewport.setValueAndLimits(currentPosition);
                viewport.setMin(currentPosition - sliderPaddingInMilliseconds);
                viewport.setMax(viewport.getMin() + scaleInMilliseconds);
            } else if (currentPosition > viewport.getMax() - sliderPaddingInMilliseconds) {
                viewport.setValueAndLimits(currentPosition);
                viewport.setMax(currentPosition + sliderPaddingInMilliseconds);
                viewport.setMin(viewport.getMax() - scaleInMilliseconds);
            } else {
                viewport.setValue(currentPosition);
            }
            updateRuler();
        });

        viewport.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                StackPane track = (StackPane) viewport.lookup(".track");
                if (track != null) {
                    viewport.widthProperty().removeListener(this);
                    track.widthProperty().addListener((ObservableValue<? extends Number> observable1, Number oldValue1, Number newValue1) -> {
                        updateRuler();
                    });
                    updateRuler();
                }
            }
        });

        positionProperty.set(System.currentTimeMillis());
        resetViewport();
    }
    
    @Override
    public final ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public LongProperty positionProperty() {
        return positionProperty;
    }

    public LongProperty sacleProperty() {
        return scaleProperty;
    }

    /**
     * @param value Milliseconds.
     */
    public void setValue(long value) {
        positionProperty.set(value);
    }

    public void setValue(Date date) {
        positionProperty.set(date.getTime());
    }

    /**
     * @param scale Seconds.
     */
    public void setScale(long scale) {
        scaleProperty.setValue(scale);
    }

    public void setScale(Scale scale) {
        setScale(scale.getSeconds());
    }

    /**
     * Repaint ruler.
     */
    private void updateRuler() {
        long scale = scaleProperty.getValue();
        long stepInSeconds;
        stepInSeconds = scale / 10;

        viewport.setMajorTickUnit(stepInSeconds * 1000);

        Platform.runLater(() -> {
            StackPane viewportTrack = (StackPane) viewport.lookup(".track");
            if (viewportTrack == null || viewportTrack.getWidth() == 0) {
                return;
            }

//            if (scale <= DateTimeSlider.Scale.MINUTE.getSeconds()) {
//                stepInSeconds = 1;
//            } else if (scale <= DateTimeSlider.Scale.HOUR.getSeconds()) {
//                stepInSeconds = 60;
//            } else  {
//                stepInSeconds = 3600;
//            }
            ruller.setWidth(viewportTrack.getWidth() - viewportTrack.getInsets().getLeft() - viewportTrack.getInsets().getRight());
            ruller.setHeight(35);

            ruller.setLayoutX(((Pane) ruller.getParent()).getWidth() / 2 - ruller.getWidth() / 2);

            double viewportMin = viewport.getMin();
            double viewportMax = viewport.getMax();
            double pixelsPerMillis = ruller.getWidth() / scale / 1000;
            double offsetToWholeSecondInMillis = 1000 - viewport.getMin() % 1000;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Double.valueOf(viewport.getMin() + offsetToWholeSecondInMillis).longValue());
            GraphicsContext rullerGc = ruller.getGraphicsContext2D();
            rullerGc.clearRect(0, 0, ruller.getWidth(), ruller.getHeight());
            rullerGc.setFill(Color.web("#555555"));
            rullerGc.setStroke(Color.web("#999999"));
            rullerGc.setTextAlign(TextAlignment.CENTER);
            rullerGc.setTextBaseline(VPos.CENTER);
            rullerGc.setLineWidth(1);
            while (calendar.getTimeInMillis() <= viewportMax) {
                double marginLeft = pixelsPerMillis * (calendar.getTimeInMillis() - viewportMin);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                rullerGc.strokeLine(marginLeft, 0, marginLeft, 5);
                rullerGc.fillText(String.format(""
                        + "%s:%s:%s"
                        + "\n"
                        + "%s.%s.%s",
                        hour, minute, second,
                        day, month, year
                ), marginLeft, 20);
                calendar.add(Calendar.SECOND, Long.valueOf(stepInSeconds).intValue());
            }
            if (onRullerUpdate != null) {
                onRullerUpdate.accept(ruller);
            }
        });
    }

    public final void resetViewport() {
        long currentPosition = positionProperty().longValue();
        long scaleInMilliseconds = scaleProperty.longValue() * 1000;
        long sliderPaddingInMilliseconds = sliderPaddingPercent * scaleInMilliseconds / 100;
        viewport.setMin(currentPosition - sliderPaddingInMilliseconds);
        viewport.setMax(viewport.getMin() + scaleInMilliseconds);
        updateRuler();
    }

    public Consumer<Canvas> getOnRullerUpdate() {
        return onRullerUpdate;
    }

    public void setOnRullerUpdate(Consumer<Canvas> onRullerUpdate) {
        this.onRullerUpdate = onRullerUpdate;
    }

    public long getMin() {
        return Double.valueOf(viewport.getMin()).longValue();
    }

    public long getMax() {
        return Double.valueOf(viewport.getMax()).longValue();
    }

    private class Viewport extends Slider {

        public Viewport() {
        }

        /**
         * Allows to set value strictly even if the value is out of limits.
         *
         * Prevents a double value change event because of setMin/setMax. This
         * methods call setValue if value is out of range.
         *
         * @param value
         */
        public void setValueAndLimits(double value) {
            setMin(value);
            setMax(value);
        }
    }
}
