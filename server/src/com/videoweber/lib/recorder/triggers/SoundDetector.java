package com.videoweber.lib.recorder.triggers;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.recorder.Trigger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class SoundDetector extends Trigger {

    public static final int DEFAULT_THRESHOLD = -40;

    /**
     * Decibels [-infinity, 0]
     */
    private int threshold = 0;

    public SoundDetector(long durationBefore, long durationAfter, int threshold) {
        super(durationBefore, durationAfter);
        if (threshold > 0) {
            throw new IllegalArgumentException();
        }
        this.threshold = threshold;
    }

    @Override
    public final MediaType getMediaType() {
        return MediaType.AUDIO;
    }

    public int getThreshold() {
        return threshold;
    }

}
