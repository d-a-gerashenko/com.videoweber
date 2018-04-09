package com.videoweber.lib.track;

import com.videoweber.lib.sampler.Sample;
import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface Track {

    /**
     * @param position
     * @return Sample at position or first sample after position. Null - if
     * there is no more samples.
     */
    public Sample getSample(Date position);

    /**
     *
     * @return Last sample from track or null.
     */
    public abstract Sample getLastSample();
}
