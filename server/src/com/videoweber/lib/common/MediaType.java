package com.videoweber.lib.common;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public enum MediaType {
    AUDIO, VIDEO, VIDEO_AND_AUDIO;

    public boolean isCompatible(MediaType type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        switch (type) {
            case AUDIO:
                if (this == VIDEO) {
                    return false;
                }
                break;
            case VIDEO:
                if (this == AUDIO) {
                    return false;
                }
                break;
        }
        return true;
    }
}
