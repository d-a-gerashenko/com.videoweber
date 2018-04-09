package com.videoweber.server.entity;

import java.io.Serializable;
import javax.persistence.*;
import com.videoweber.lib.common.HasMediaType;
import com.videoweber.lib.common.MediaType;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class ChannelEntity implements Serializable, HasMediaType {

    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    @Id
    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    //--------------------------------------------------------------------------
    @Column(length = 50)
    private String title = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    //--------------------------------------------------------------------------
    @Column(name = "_order")
    private int order = 0;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    //--------------------------------------------------------------------------
    @ManyToOne
    private SourceEntity videoSource = null;

    public SourceEntity getVideoSource() {
        return videoSource;
    }

    public void setVideoSource(SourceEntity videoSource) {
        this.videoSource = videoSource;
    }

    //--------------------------------------------------------------------------
    @ManyToOne
    private SourceEntity audioSource = null;

    public SourceEntity getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(SourceEntity audioSource) {
        this.audioSource = audioSource;
    }

    //--------------------------------------------------------------------------
    @Override
    public MediaType getMediaType() {
        if (videoSource == null && audioSource == null) {
            throw new RuntimeException("Can't get a media type of the channel entity. Both sources are null.");
        }

        if (videoSource == null) {
            return MediaType.AUDIO;
        } else if (audioSource == null) {
            return MediaType.VIDEO;
        } else {
            return MediaType.VIDEO_AND_AUDIO;
        }
    }
}
