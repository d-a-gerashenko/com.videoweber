package com.videoweber.lib.track.player;

import java.util.Objects;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public enum ExtendedPlayerState {
    PLAYING, PAUSED, STOPPED, UNREACHED;

    public TrackPlayerState toPlayerState() {
        switch (this) {
            case PLAYING:
                return TrackPlayerState.PLAYING;
            case PAUSED:
                return TrackPlayerState.PAUSED;
            default:
                return TrackPlayerState.STOPPED;
        }
    }

    public static ExtendedPlayerState fromPlayerState(TrackPlayerState state) {
        Objects.requireNonNull(state);
        switch (state) {
            case PLAYING:
                return PLAYING;
            case PAUSED:
                return PAUSED;
            default:
                return STOPPED;
        }
    }
}
