package com.videoweber.lib.track.player.model;

import com.videoweber.lib.track.player.TrackPlayerState;
import java.util.Objects;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class TrackPlayerModel implements
        ReadableTrackPlayerModelInterface,
        WritableTrackPlayerModelInterface {

    private TrackPlayerState state;
    /**
     * In microseconds.
     */
    private long position;
    private float volume;
    private TrackPlayerState unreachedState;

    public TrackPlayerModel() {
        state = TrackPlayerState.STOPPED;
        position = 0;
        volume = 1;
        unreachedState = null;
    }

    @Override
    public TrackPlayerState getState() {
        return state;
    }

    @Override
    public void setState(TrackPlayerState state) {
        TrackPlayerModelValidator.validateState(state);
        this.state = state;
    }

    /**
     * In microseconds.
     *
     * @return
     */
    @Override
    public long getPosition() {
        return position;
    }

    /**
     * @param position In microseconds.
     */
    @Override
    public void setPosition(long position) {
        TrackPlayerModelValidator.validatePosition(position);
        this.position = position;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setVolume(float volume) {
        TrackPlayerModelValidator.validateVolume(volume);
        this.volume = volume;
    }

    @Override
    public TrackPlayerState getUnreachedState() {
        return unreachedState;
    }

    @Override
    public void setUnreachedState(TrackPlayerState unreachedState) {
        this.unreachedState = unreachedState;
    }

    public void updateWith(TrackPlayerModelUpdate modelUpdate) {
        Objects.requireNonNull(modelUpdate);
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

    public void updateWith(TrackPlayerModel model) {
        Objects.requireNonNull(model);
        setState(model.getState());
        setPosition(model.getPosition());
        setVolume(model.getVolume());
        setUnreachedState(model.getUnreachedState());
    }

}
