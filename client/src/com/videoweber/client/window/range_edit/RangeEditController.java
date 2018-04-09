package com.videoweber.client.window.range_edit;

import com.videoweber.lib.JavaFX.DateTimeSelector;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.common.ArrayAccess.ArrayAccess;
import com.videoweber.lib.gui.Gui;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class RangeEditController extends WindowController {

    @FXML
    private Parent root;

    @FXML
    private FlowPane beginPane;
    private final DateTimeSelector beginDateTimeSelector = new DateTimeSelector();

    @FXML
    private FlowPane endPane;
    private final DateTimeSelector endDateTimeSelector = new DateTimeSelector();

    private Range oldRange = null;
    private BiFunction<Range, Range, String> rangeHandler = null;

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void onWindowInitialized() {
        beginPane.getChildren().add(beginDateTimeSelector);
        endPane.getChildren().add(endDateTimeSelector);
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        rangeHandler = ArrayAccess.arrayItem(0, BiFunction.class, parameters);
        if (rangeHandler == null) {
            throw new NullPointerException();
        }

        oldRange = ArrayAccess.arrayItem(1, Range.class, parameters, null);

        if (oldRange != null) {
            beginDateTimeSelector.valueProperty().set(oldRange.getBegin().getTime());
            endDateTimeSelector.valueProperty().set(oldRange.getEnd().getTime());
        } else {
            beginDateTimeSelector.valueProperty().set(System.currentTimeMillis());
            beginDateTimeSelector.resetMilliseconds();
            endDateTimeSelector.valueProperty().set(System.currentTimeMillis());
            endDateTimeSelector.resetMilliseconds();
        }
    }

    public void saveAction() {
        if (endDateTimeSelector.valueProperty().get() - beginDateTimeSelector.valueProperty().get() < 60 * 1000) { // 1 minute
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Неправильный формат интервала",
                    "Интервал не может быть меньше 1 минуты или отрицательным."
            ).show();
            return;
        }
        Range newRange = new Range(
                new Date(beginDateTimeSelector.valueProperty().get()),
                new Date(endDateTimeSelector.valueProperty().get())
        );
        String error = rangeHandler.apply(oldRange, newRange);
        if (error != null) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Неправильный формат интервала",
                    error
            ).show();
            return;
        }
        getStage().close();
    }
}
