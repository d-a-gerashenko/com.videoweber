package com.videoweber.server.window.trigger_create;

import com.videoweber.lib.recorder.Trigger;
import com.videoweber.lib.recorder.triggers.MotionDetector;
import com.videoweber.lib.recorder.triggers.SoundDetector;
import com.videoweber.server.entity.ChannelEntity;
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
import java.util.UUID;
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
public class TriggerCreateController extends WindowController {

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

    private ChannelEntity detachedChannelEntity;

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
        timeBeforeTriggerSlider.setValue(10);
        timeAfterTriggerSlider.setValue(20);
        motionMaxThresholdSlider.setValue(100);
        motionMinThresholdSlider.setValue(0);
        soundThresholdSlider.setValue(0);

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
        getStage().setTitle(
                String.format(
                        "Добавление условий записи на канал \"%s\"",
                        detachedChannelEntity.getTitle()
                )
        );
    }

    private TriggerEntity createTriggerEntity() {
        TriggerEntity triggerEntity;
        switch (triggerTabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                TriggerMotionEntity triggerMotionEntity = new TriggerMotionEntity();
                triggerMotionEntity.setThresholdMin(
                        MotionDetector.DEFAULT_THRESHOLD_MIN + motionMinThresholdSlider.getValue() * (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN) / 100
                );
                triggerMotionEntity.setThresholdMax(
                        MotionDetector.DEFAULT_THRESHOLD_MIN + motionMaxThresholdSlider.getValue() * (MotionDetector.DEFAULT_THRESHOLD_MAX - MotionDetector.DEFAULT_THRESHOLD_MIN) / 100
                );
                triggerEntity = triggerMotionEntity;
                break;
            case 1:
                TriggerSoundEntity triggerSoundEntity = new TriggerSoundEntity();
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
        return triggerEntity;
    }

    public void saveAction() {
        TriggerEntity triggerEntity = createTriggerEntity();
        triggerEntity.setChannel(detachedChannelEntity);
        getServiceContainer().getService(HibernateService.class).save(triggerEntity);

        getService(EventService.class).trigger(new EntityEvent(
                TriggerEntity.class,
                EntityOperation.CREATE,
                triggerEntity.getId()
        ));

        getService(EventService.class).trigger(new EntityEvent(
                ChannelEntity.class,
                EntityOperation.UPDATE,
                detachedChannelEntity.getUuid()
        ));

        getStage().close();
    }

    public void testAction() {
        Trigger trigger = getService(TriggerManagerService.class)
                .createTrigger(createTriggerEntity());
        HashMap<String, Object> options = new HashMap<String, Object>() {
            {
                put("triggers", new Trigger[]{trigger});
            }
        };
        Object[] parameters = new Object[]{
            getService(ChannelManagerService.class)
            .createChannel(detachedChannelEntity),
            options
        };
        getService(WindowService.class).openWindow(
                ChannelViewWindow.class,
                parameters
        );
    }

}
