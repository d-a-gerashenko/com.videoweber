package com.videoweber.server.window.trigger_edit;

import com.videoweber.lib.recorder.Trigger;
import com.videoweber.lib.recorder.triggers.MotionDetector;
import com.videoweber.lib.recorder.triggers.SoundDetector;
import com.videoweber.server.entity.TriggerEntity;
import com.videoweber.server.entity.TriggerMotionEntity;
import com.videoweber.server.entity.TriggerSoundEntity;
import com.videoweber.server.service.ChannelManagerService;
import com.videoweber.server.service.HibernateService;
import com.videoweber.server.service.TriggerManagerService;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.app.service.event_service.EventService;
import com.videoweber.lib.app.service.event_service.entity.EntityEvent;
import com.videoweber.lib.app.service.event_service.entity.EntityOperation;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.server.window.channel_view.ChannelViewWindow;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TriggerEditController extends WindowController {

    @FXML
    private Parent root;
    @FXML
    private Slider timeBeforeTriggerSlider;
    @FXML
    private Slider timeAfterTriggerSlider;
    @FXML
    private Slider motionMinThresholdSlider;
    @FXML
    private Slider motionMaxThresholdSlider;
    @FXML
    private Slider soundThresholdSlider;
    @FXML
    private TabPane triggerTabPane;

    private TriggerEntity detachedTriggerEntity;

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        motionMinThresholdSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (motionMaxThresholdSlider.getValue() - newValue.doubleValue() < 10) {
                motionMinThresholdSlider.setValue(motionMaxThresholdSlider.getValue() - 10);
            }
        });
        motionMaxThresholdSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.doubleValue() - motionMinThresholdSlider.getValue() < 10) {
                motionMaxThresholdSlider.setValue(motionMinThresholdSlider.getValue() + 10);
            }
        });
    }

    @Override
    public void onWindowOpened(Object[] parameters) {
        if (parameters == null
                || parameters.length == 0
                || !(parameters[0] instanceof Long)) {
            throw new IllegalArgumentException();
        }
        Long entityId = (Long) parameters[0];
        detachedTriggerEntity = getService(HibernateService.class)
                .getRepository(TriggerEntity.class)
                .get(entityId);
        if (detachedTriggerEntity == null) {
            throw new NullPointerException();
        }
        getStage().setTitle(
                String.format(
                        "Редактирование условия записи на канал \"%s\"",
                        detachedTriggerEntity.getChannel().getTitle()
                )
        );
        updateUI();
    }

    private void updateUI() {
        timeBeforeTriggerSlider.setValue(detachedTriggerEntity.getDurationBefore() / 1000);
        timeAfterTriggerSlider.setValue(detachedTriggerEntity.getDurationAfter() / 1000);

        if (detachedTriggerEntity instanceof TriggerMotionEntity) {
            triggerTabPane.getSelectionModel().select(0);
            TriggerMotionEntity triggerMotionEntity = (TriggerMotionEntity) detachedTriggerEntity;
            motionMaxThresholdSlider.setValue(
                    100 * (triggerMotionEntity.getThresholdMax() - MotionDetector.DEFAULT_THRESHOLD_MIN) / (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN)
            );
            motionMinThresholdSlider.setValue(
                    100 * (triggerMotionEntity.getThresholdMin() - MotionDetector.DEFAULT_THRESHOLD_MIN) / (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN)
            );
        } else if (detachedTriggerEntity instanceof TriggerSoundEntity) {
            triggerTabPane.getSelectionModel().select(1);
            TriggerSoundEntity triggerSoundEntity = (TriggerSoundEntity) detachedTriggerEntity;
            soundThresholdSlider.setValue(
                    100 * (triggerSoundEntity.getThreshold() - SoundDetector.DEFAULT_THRESHOLD) / (0 - SoundDetector.DEFAULT_THRESHOLD)
            );
        }
        throw new RuntimeException("Unsupported TriggerEntity class: " + detachedTriggerEntity.getClass().getName());
    }

    private void updateTriggerEntity() {
        TriggerEntity triggerEntity;
        switch (triggerTabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                TriggerMotionEntity triggerMotionEntity = (TriggerMotionEntity) detachedTriggerEntity;
                triggerMotionEntity.setThresholdMin(
                        MotionDetector.DEFAULT_THRESHOLD_MIN + motionMinThresholdSlider.getValue() * (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN) / 100
                );
                triggerMotionEntity.setThresholdMax(
                        MotionDetector.DEFAULT_THRESHOLD_MIN + motionMaxThresholdSlider.getValue() * (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN) / 100
                );
                triggerEntity = triggerMotionEntity;
                break;
            case 1:
                TriggerSoundEntity triggerSoundEntity = (TriggerSoundEntity) detachedTriggerEntity;
                triggerSoundEntity.setThreshold(Double.valueOf(
                        SoundDetector.DEFAULT_THRESHOLD + soundThresholdSlider.getValue() * (0 - SoundDetector.DEFAULT_THRESHOLD) / 100
                ).intValue());
                triggerEntity = triggerSoundEntity;
                break;
            default:
                throw new RuntimeException("Unsupported tab index:" + triggerTabPane.getSelectionModel().getSelectedIndex());
        }
        triggerEntity.setDurationBefore(Double.valueOf(timeBeforeTriggerSlider.getValue() * 1000).intValue());
        triggerEntity.setDurationAfter(Double.valueOf(timeAfterTriggerSlider.getValue() * 1000).intValue());
    }

    public void saveAction() {
        updateTriggerEntity();
        getServiceContainer().getService(HibernateService.class).update(detachedTriggerEntity);

        getService(EventService.class).trigger(new EntityEvent(
                TriggerEntity.class,
                EntityOperation.UPDATE,
                detachedTriggerEntity.getId()
        ));

        getStage().close();
    }

    public void testAction() {
        updateTriggerEntity();
        Trigger trigger = getService(TriggerManagerService.class)
                .createTrigger(detachedTriggerEntity);
        HashMap<String, Object> options = new HashMap<String, Object>() {
            {
                put("triggers", new Trigger[]{trigger});
            }
        };
        Object[] parameters = new Object[]{
            getService(ChannelManagerService.class)
            .createChannel(detachedTriggerEntity.getChannel()),
            options
        };
        getService(WindowService.class).openWindow(
                ChannelViewWindow.class,
                parameters
        );
    }

}
