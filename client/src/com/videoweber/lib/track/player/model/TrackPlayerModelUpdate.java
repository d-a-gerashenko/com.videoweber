package com.videoweber.lib.track.player.model;

import com.videoweber.lib.track.player.TrackPlayerState;
import java.util.Objects;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class TrackPlayerModelUpdate {

    private TrackPlayerState state;
    private boolean stateChanged;

    /**
     * In microseconds.
     */
    private long position;
    private boolean positionChanged;

    private float volume;
    private boolean volumeChanged;

    public TrackPlayerModelUpdate() {
        reset();
    }

    public synchronized void reset() {
        state = null;
        stateChanged = false;
        position = -1;
        positionChanged = false;
        volume = -1;
        volumeChanged = false;
    }

    public TrackPlayerState getState() {
        if (!stateChanged) {
            throw new IllegalStateException();
        }
        return state;
    }

    public void setState(TrackPlayerState state) {
        TrackPlayerModelValidator.validateState(state);
        this.state = state;
        stateChanged = true;
    }

    public boolean isStateChanged() {
        return stateChanged;
    }

    /**
     * @return In microseconds.
     */
    public long getPosition() {
        if (!positionChanged) {
            throw new IllegalStateException();
        }
        return position;
    }

    /**
     * @param position In microseconds.
     */
    public void setPosition(long position) {
        TrackPlayerModelValidator.validatePosition(position);
        this.position = position;
        positionChanged = true;
    }

    public synchronized boolean isPositionChanged() {
        return positionChanged;
    }

    /**
     * @return From 0 to 1.
     */
    public float getVolume() {
        if (!volumeChanged) {
            throw new IllegalStateException();
        }
        return volume;
    }

    /**
     * @param volume From 0 to 1.
     */
    public void setVolume(float volume) {
        TrackPlayerModelValidator.validateVolume(volume);
        this.volume = volume;
        volumeChanged = true;
    }

    public boolean isVolumeChanged() {
        return volumeChanged;
    }

    public boolean isChanged() {
        return isStateChanged() || isPositionChanged() || isVolumeChanged();
    }

    public void updateWith(TrackPlayerModelUpdate modelUpdate) {
        Objects.requireNonNull(modelUpdate);
        if (this == modelUpdate) {
            throw new IllegalArgumentException("ModelUpdate can't be updated with itself.");
        }
        if (modelUpdate.isChanged()) {
            if (modelUpdate.isStateChanged()) {
                setState(modelUpdate.getState());
            }
            if (modelUpdate.isPositionChanged()) {
                setPosition(modelUpdate.getPosition());
            }
            if (modelUpdate.isVolumeChanged()) {
                setVolume(modelUpdate.getVolume());
            }
        }
    }

}
