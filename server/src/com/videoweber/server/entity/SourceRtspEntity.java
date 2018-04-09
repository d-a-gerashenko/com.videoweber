package com.videoweber.server.entity;

import com.videoweber.lib.common.MediaType;
import javax.persistence.*;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class SourceRtspEntity extends SourceEntity {

    @Column
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    //--------------------------------------------------------------------------
    @Column
    private MediaType mediaType;

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
