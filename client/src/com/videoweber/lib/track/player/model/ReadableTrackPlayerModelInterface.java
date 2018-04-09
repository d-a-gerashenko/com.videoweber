package com.videoweber.lib.track.player.model;

import com.videoweber.lib.track.player.TrackPlayerState;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface ReadableTrackPlayerModelInterface {

    public TrackPlayerState getState();

    /**
     * @return In microseconds.
     */
    public long getPosition();

    /**
     * @return From 0 to 1.
     */
    public float getVolume();

    public TrackPlayerState getUnreachedState();
}
