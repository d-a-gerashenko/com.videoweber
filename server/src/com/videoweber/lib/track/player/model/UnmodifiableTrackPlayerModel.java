package com.videoweber.lib.track.player.model;

import com.videoweber.lib.track.player.TrackPlayerState;
import java.util.Objects;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UnmodifiableTrackPlayerModel implements ReadableTrackPlayerModelInterface {

    private final ReadableTrackPlayerModelInterface model;

    public UnmodifiableTrackPlayerModel(ReadableTrackPlayerModelInterface model) {
        Objects.requireNonNull(model);
        this.model = model;
    }

    @Override
    public TrackPlayerState getState() {
        return model.getState();
    }

    /**
     * @return In microseconds.
     */
    @Override
    public long getPosition() {
        return model.getPosition();
    }

    /**
     * @return From 0 to 1.
     */
    @Override
    public float getVolume() {
        return model.getVolume();
    }

    @Override
    public TrackPlayerState getUnreachedState() {
        return model.getUnreachedState();
    }

}
