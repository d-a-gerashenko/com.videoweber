package com.videoweber.lib.JavaFX;

import java.time.LocalDate;
import java.util.Calendar;
import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 *
 * @author gda
 */
public class DateTimeSelector extends Pane {

    /**
     * In milliseconds.
     */
    private final LongProperty valueProperty = new LongPropertyBase() {

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "datetime";
        }

    };
    private final DatePicker date = new DatePicker();
    private final ChoiceBox<String> hour = new ChoiceBox<>();
    private final ChoiceBox<String> minute = new ChoiceBox<>();
    private final ChoiceBox<String> second = new ChoiceBox<>();
    private boolean isChanging = false;

    public DateTimeSelector() {
        for (int i = 0; i <= 23; i++) {
            hour.getItems().add(String.format("%02d", i));
        }
        for (int i = 0; i <= 59; i++) {
            minute.getItems().add(String.format("%02d", i));
            second.getItems().add(String.format("%02d", i));
        }
        valueProperty.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateControls();
        });
        date.valueProperty().addListener((ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
            updateValue();
        });
        hour.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            updateValue();
        });
        minute.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            updateValue();
        });
        second.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            updateValue();
        });
        valueProperty.set(System.currentTimeMillis());
        this.getChildren().addAll(
                new HBox(5,
                        date,
                        new Label("-"),
                        hour,
                        new Label("час."),
                        minute,
                        new Label("мин."),
                        second,
                        new Label("сек.")
                ) {
            {
                setAlignment(Pos.CENTER);
            }
        }
        );
    }
    
    @Override
    public final ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public LongProperty valueProperty() {
        return valueProperty;
    }
    
    public void resetMilliseconds() {
        valueProperty.setValue(resetMilliseconds(valueProperty.getValue()));
    }
    
    private static long resetMilliseconds(long timeInMilliseconds) {
        return 1000 * (timeInMilliseconds / 1000);
    }

    private void updateControls() {
        isChanging = true;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(valueProperty.longValue());
        date.setValue(LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        ));
        hour.getSelectionModel().select(calendar.get(Calendar.HOUR_OF_DAY));
        minute.getSelectionModel().select(calendar.get(Calendar.MINUTE));
        second.getSelectionModel().select(calendar.get(Calendar.SECOND));
        isChanging = false;
    }

    private void updateValue() {
        if (isChanging) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                date.getValue().getYear(),
                date.getValue().getMonthValue() - 1,
                date.getValue().getDayOfMonth(),
                Integer.parseInt(hour.getValue()),
                Integer.parseInt(minute.getValue()),
                Integer.parseInt(second.getValue())
        );
        valueProperty.set(resetMilliseconds(calendar.getTimeInMillis()));
    }

    public DatePicker getDate() {
        return date;
    }

    public ChoiceBox<String> getHour() {
        return hour;
    }

    public ChoiceBox<String> getMinute() {
        return minute;
    }

    public ChoiceBox<String> getSecond() {
        return second;
    }

}
