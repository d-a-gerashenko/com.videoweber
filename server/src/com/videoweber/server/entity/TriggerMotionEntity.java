package com.videoweber.server.entity;

import javax.persistence.*;
import com.videoweber.lib.common.MediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class TriggerMotionEntity extends TriggerEntity {

    @Column
    private double thresholdMin = 0;

    public double getThresholdMin() {
        return thresholdMin;
    }

    public void setThresholdMin(double thresholdMin) {
        checkThreshold(thresholdMin);
        this.thresholdMin = thresholdMin;
    }

    //--------------------------------------------------------------------------
    @Column
    private double thresholdMax = 0;

    public double getThresholdMax() {
        return thresholdMax;
    }

    public void setThresholdMax(double thresholdMax) {
        checkThreshold(thresholdMax);
        this.thresholdMax = thresholdMax;
    }

    private void checkThreshold(double threshold) {
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException();
        }
    }

    //--------------------------------------------------------------------------
    @Override
    public final MediaType getMediaType() {
        return MediaType.VIDEO;
    }
}
