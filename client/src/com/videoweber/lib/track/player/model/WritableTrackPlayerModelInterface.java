package com.videoweber.lib.track.player.model;

import com.videoweber.lib.track.player.TrackPlayerState;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface WritableTrackPlayerModelInterface {

    public void setState(TrackPlayerState state);

    /**
     * @param position In microseconds.
     */
    public void setPosition(long position);

    /**
     * @param volume From 0 to 1.
     */
    public void setVolume(float volume);

    public void setUnreachedState(TrackPlayerState unreachedState);

}
