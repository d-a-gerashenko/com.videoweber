package com.videoweber.server.service;

import com.videoweber.lib.app.service.TempDirService;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.engines.javacv.FfmpegSoundDetector;
import com.videoweber.lib.engines.javacv.OpencvMotionDetector;
import com.videoweber.lib.recorder.Trigger;
import com.videoweber.lib.recorder.triggers.MotionDetector;
import com.videoweber.server.entity.TriggerEntity;
import com.videoweber.server.entity.TriggerMotionEntity;
import com.videoweber.server.entity.TriggerSoundEntity;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TriggerManagerService extends Service {

    public TriggerManagerService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public Trigger createTrigger(TriggerEntity triggerEntity) {
        if (triggerEntity == null) {
            throw new NullPointerException();
        }
        if (triggerEntity instanceof TriggerMotionEntity) {
            TriggerMotionEntity triggerMotionEntity = (TriggerMotionEntity)triggerEntity;
            MotionDetector motionDetector = new OpencvMotionDetector(
                    triggerEntity.getDurationBefore(),
                    triggerEntity.getDurationAfter(),
                    triggerMotionEntity.getThresholdMin(),
                    triggerMotionEntity.getThresholdMax()
            );
            return motionDetector;
        }
        if (triggerEntity instanceof TriggerSoundEntity) {
            TriggerSoundEntity triggerSoundEntity = (TriggerSoundEntity)triggerEntity;
            FfmpegSoundDetector soundDetector = new FfmpegSoundDetector(
                    triggerEntity.getDurationBefore(),
                    triggerEntity.getDurationAfter(),
                    triggerSoundEntity.getThreshold(),
                    getServiceContainer().getService(TempDirService.class).createTempDir()
            );
            return soundDetector;

        }
        throw new RuntimeException("Unsupported TriggerEntity class: " + triggerEntity.getClass().getName());
    }

}
