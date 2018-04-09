package com.videoweber.lib.track.player.model;

import com.videoweber.lib.track.player.TrackPlayerState;
import java.util.Objects;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface TrackPlayerModelValidator {

    public static void validateState(TrackPlayerState state) {
        Objects.requireNonNull(state);
    }

    public static void validatePosition(long position) {
        if (position < 0) {
            throw new IllegalArgumentException();
        }
    }

    public static void validateVolume(float volume) {
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException();
        }
    }
}
