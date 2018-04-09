package com.videoweber.lib.channel.sources;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.channel.Source;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Rtsp extends Source {

    private MediaType mediaType;

    private String uri;

    public Rtsp(MediaType mediaType, String uri) {
        if (mediaType == null || uri == null) {
            throw new IllegalArgumentException();
        }
        this.mediaType = mediaType;
        this.uri = uri;
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    public String getUri() {
        return uri;
    }

}
