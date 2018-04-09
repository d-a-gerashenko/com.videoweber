package com.videoweber.lib.recorder.triggers;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.recorder.Trigger;
import com.videoweber.lib.sampler.Sample;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class AlwaysTrue extends Trigger {

    public AlwaysTrue(long durationBefore, long durationAfter) {
        super(durationBefore, durationAfter);
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.VIDEO_AND_AUDIO;
    }

    @Override
    public boolean _check(Sample sample) {
        return true;
    }

}
