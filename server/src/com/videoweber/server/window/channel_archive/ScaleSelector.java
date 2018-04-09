package com.videoweber.server.window.channel_archive;

import com.videoweber.lib.JavaFX.DateTimeSlider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

/**
 *
 * @author gda
 */
public class ScaleSelector extends ChoiceBox<DateTimeSlider.Scale> {

    private static final Map<DateTimeSlider.Scale, String> VERBOUSE_SCALE = Collections.unmodifiableMap(new HashMap<DateTimeSlider.Scale, String>() {
        {
            put(DateTimeSlider.Scale.HOUR_24, "24 часа");
            put(DateTimeSlider.Scale.HOUR_8, "8 часов");
            put(DateTimeSlider.Scale.HOUR_3, "3 часа");
            put(DateTimeSlider.Scale.HOUR_2, "2 часа");
            put(DateTimeSlider.Scale.HOUR, "1 час");
            put(DateTimeSlider.Scale.MINUTE_30, "30 минут");
            put(DateTimeSlider.Scale.MINUTE_10, "10 минут");
            put(DateTimeSlider.Scale.MINUTE, "1 минута");
            put(DateTimeSlider.Scale.SECOND_30, "30 секунд");
        }
    });

    public ScaleSelector() {
        getItems().addAll(Arrays.asList(DateTimeSlider.Scale.values()));
        setConverter(new StringConverter<DateTimeSlider.Scale>() {
            @Override
            public String toString(DateTimeSlider.Scale scale) {
                return VERBOUSE_SCALE.get(scale);
            }

            @Override
            public DateTimeSlider.Scale fromString(String string) {
                int selectedIndex = getSelectionModel().getSelectedIndex();
                if (selectedIndex == 0) {
                    return null;
                }
                return getItems().get(selectedIndex - 1);
            }
        });
        getSelectionModel().select(DateTimeSlider.Scale.HOUR);
    }

}
