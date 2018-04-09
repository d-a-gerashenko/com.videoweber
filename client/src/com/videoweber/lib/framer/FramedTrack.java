package com.videoweber.lib.framer;

import com.videoweber.lib.track.Track;
import com.videoweber.lib.sampler.Sample;
import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FramedTrack implements FramedTrackInterface {

    private final Track track;
    private final Framer framer;

    public FramedTrack(Track track, Framer framer) {
        if (track == null || framer == null) {
            throw new IllegalArgumentException();
        }
        this.track = track;
        this.framer = framer;
    }

    public Track getTrack() {
        return track;
    }

    public Framer getFramer() {
        return framer;
    }

    @Override
    public Frame getFrame(Date position) {
        Sample sample = track.getSample(position);
        if (sample == null) {
            return null;
        }
        return framer.cutFrame(sample);
    }

    @Override
    public Frame getLastFrame() {
        Sample lastSample = track.getLastSample();
        if (lastSample == null) {
            return null;
        }
        return framer.cutFrame(lastSample);
    }
}
