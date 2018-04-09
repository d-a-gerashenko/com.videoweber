package com.videoweber.lib.track;

import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.sampler.SampleHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TempTrack implements Track, SampleHandler {

    private final ArrayList<Sample> samples = new ArrayList<>();
    private volatile int samplesCountLimit = 0;

    public TempTrack() {
        this(10);
    }

    public TempTrack(int samplesCountLimit) {
        setSamplesCountLimit(samplesCountLimit);
    }

    @Override
    public synchronized Sample getSample(Date position) {
        Sample result = null;
        for (Sample item : samples) {
            if (!item.getEnd().before(position)) {
                if (result == null || item.getBegin().before(result.getBegin())) {
                    result = item;
                }
            }
        }
        return result;
    }

    @Override
    public synchronized void onSample(Sample sample) {
        Objects.requireNonNull(sample);

        while (samples.size() >= samplesCountLimit) {
            removeFirstSample();
        }

        samples.add(sample);
    }

    @Override
    public synchronized Sample getLastSample() {
        Sample result = null;
        for (Sample item : samples) {
            if (result == null || result.getBegin().before(item.getBegin())) {
                result = item;
            }
        }
        return result;
    }

    private synchronized void removeFirstSample() {
        Sample result = null;
        for (Sample item : samples) {
            if (result == null || item.getBegin().before(result.getBegin())) {
                result = item;
            }
        }
        if (result != null) {
            samples.remove(result);
            result.getFile().delete();
        }
    }

    public int getSamplesCountLimit() {
        return samplesCountLimit;
    }

    public final void setSamplesCountLimit(int samplesCountLimit) {
        if (samplesCountLimit < 1) {
            throw new IllegalArgumentException("samplesCountLimit shouldn't be less than 1.");
        }
        this.samplesCountLimit = samplesCountLimit;
    }

}
