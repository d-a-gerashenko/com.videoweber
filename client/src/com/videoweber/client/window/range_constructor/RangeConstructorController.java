package com.videoweber.client.window.range_constructor;

import com.videoweber.client.window.range_edit.Range;
import com.videoweber.lib.JavaFX.DateTimeSelector;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.common.ArrayAccess.ArrayAccess;
import com.videoweber.lib.gui.Gui;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class RangeConstructorController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private FlowPane beginPane;
    private final DateTimeSelector beginDateTimeSelector = new DateTimeSelector() {
        {
            resetMilliseconds();
        }
    };

    @FXML
    private FlowPane endPane;
    private final DateTimeSelector endDateTimeSelector = new DateTimeSelector() {
        {
            resetMilliseconds();
        }
    };

    @FXML
    private ChoiceBox<Frequency> frequencyChoiceBox;

    private static enum Frequency {
        MINUTES_10(600000, "10 минут"),
        MINUTES_20(1200000, "20 минут"),
        MINUTES_30(1800000, "30 минут"),
        HOURS_1(3600000, "1 час");

        /**
         * milliseconds
         */
        private final long value;
        private final String verbalName;

        /**
         * @param value milliseconds
         * @param verbalName
         */
        Frequency(long value, String verbalName) {
            this.value = value;
            this.verbalName = verbalName;
        }

        public long getValue() {
            return value;
        }

        public String getVerbalName() {
            return verbalName;
        }

        public static Frequency createFromVerbalName(String verbalName) {
            for (Frequency frequency : Frequency.values()) {
                if (frequency.getVerbalName().equals(verbalName)) {
                    return frequency;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    @FXML
    private ChoiceBox<Duration> durationChoiceBox;

    private static enum Duration {
        MINUTES_1(60000, "1 минута"),
        MINUTES_2(120000, "2 минуты"),
        MINUTES_3(180000, "3 минуты"),
        MINUTES_4(240000, "4 минуты"),
        MINUTES_5(300000, "5 минут");

        /**
         * milliseconds
         */
        private final long value;
        private final String verbalName;

        /**
         * @param value milliseconds
         * @param verbalName
         */
        Duration(long value, String verbalName) {
            this.value = value;
            this.verbalName = verbalName;
        }

        public long getValue() {
            return value;
        }

        public String getVerbalName() {
            return verbalName;
        }

        public static Duration createFromVerbalName(String verbalName) {
            for (Duration duration : Duration.values()) {
                if (duration.getVerbalName().equals(verbalName)) {
                    return duration;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    private Consumer<Set<Range>> rangesHandler = null;

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void onWindowInitialized() {
        beginPane.getChildren().add(beginDateTimeSelector);
        endPane.getChildren().add(endDateTimeSelector);
        frequencyChoiceBox.setConverter(new StringConverter<Frequency>() {
            @Override
            public String toString(Frequency object) {
                return object.getVerbalName();
            }

            @Override
            public Frequency fromString(String string) {
                return Frequency.createFromVerbalName(string);
            }
        });
        frequencyChoiceBox.getItems().addAll(Frequency.values());
        durationChoiceBox.setConverter(new StringConverter<Duration>() {
            @Override
            public String toString(Duration object) {
                return object.getVerbalName();
            }

            @Override
            public Duration fromString(String string) {
                return Duration.createFromVerbalName(string);
            }
        });
        durationChoiceBox.getItems().addAll(Duration.values());
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        rangesHandler = ArrayAccess.arrayItem(0, Consumer.class, parameters);
        if (rangesHandler == null) {
            throw new NullPointerException();
        }
    }

    public void saveAction() {
        if (frequencyChoiceBox.getValue() == null) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Не выбрана частота"
            ).show();
            return;
        }
        if (durationChoiceBox.getValue() == null) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Не выбрана длительность"
            ).show();
            return;
        }

        long begin = beginDateTimeSelector.valueProperty().get();
        long end = endDateTimeSelector.valueProperty().get();
        long frequency = frequencyChoiceBox.getValue().getValue();
        long duration = durationChoiceBox.getValue().getValue();

        if (end - begin < duration) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Длина промежутка меньше длительности интервала",
                    "Неправильно указаны границы промежутка или длительность интервала.\n"
                    + "Длина промежутка должна быть больше длительности интервала."
            ).show();
            return;
        }
        Set<Range> ranges = new HashSet<>();
        for (long position = begin;
                position + duration <= end;
                position += frequency) {
            ranges.add(
                    new Range(
                            new Date(position),
                            new Date(position + duration)
                    )
            );
        }
        rangesHandler.accept(ranges);
        getStage().close();
    }
}
