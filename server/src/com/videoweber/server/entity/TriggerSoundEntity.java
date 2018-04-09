package com.videoweber.server.entity;

import javax.persistence.*;
import com.videoweber.lib.common.MediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class TriggerSoundEntity extends TriggerEntity {

    @Column
    private int threshold = 0;

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        if (threshold > 0) {
            throw new IllegalArgumentException();
        }
        this.threshold = threshold;
    }

    //--------------------------------------------------------------------------
    @Override
    public final MediaType getMediaType() {
        return MediaType.AUDIO;
    }
}
