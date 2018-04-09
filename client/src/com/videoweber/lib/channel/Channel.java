package com.videoweber.lib.channel;

import com.videoweber.lib.common.HasMediaType;
import com.videoweber.lib.common.MediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Channel implements HasMediaType {

    private final Source videoSource;
    private final Source audioSource;

    public Channel(Source videoSource, Source audioSource) {
        if (videoSource == null && audioSource == null) {
            throw new IllegalArgumentException("Channel should have at lest one source.");
        }
        if (videoSource != null && !videoSource.getMediaType().isCompatible(MediaType.VIDEO)) {
            throw new IllegalArgumentException("Type of videoSource is not compatible with VIDEO type.");
        }
        if (audioSource != null && !audioSource.getMediaType().isCompatible(MediaType.AUDIO)) {
            throw new IllegalArgumentException("Type of audioSource is not compatible with AUDIO type.");
        }
        this.videoSource = videoSource;
        this.audioSource = audioSource;
    }

    public Source getVideoSource() {
        return videoSource;
    }

    public Source getAudioSource() {
        return audioSource;
    }

    @Override
    public MediaType getMediaType() {
        if (videoSource == null) {
            return MediaType.AUDIO;
        } else if (audioSource == null) {
            return MediaType.VIDEO;
        } else {
            return MediaType.VIDEO_AND_AUDIO;
        }
    }

}
