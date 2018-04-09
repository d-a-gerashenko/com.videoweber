package com.videoweber.lib.recorder;

import com.videoweber.lib.sampler.Sample;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface RecordHandler {

    public void onRecord(Sample sample);

    public default void onRecordStop() {
    }

}
