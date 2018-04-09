package com.videoweber.client.entity;

import java.io.Serializable;
import javax.persistence.*;
import com.videoweber.lib.common.HasMediaType;
import com.videoweber.lib.common.MediaType;
import java.util.Date;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class SampleEntity implements Serializable, HasMediaType {

    public SampleEntity() {
        this.deleted = null;
    }

    //--------------------------------------------------------------------------
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    @Id
    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    //--------------------------------------------------------------------------
    @Column(columnDefinition = "BINARY(16)")
    private UUID remoteUuid;

    public UUID getRemoteUuid() {
        return remoteUuid;
    }

    public void setRemoteUuid(UUID remoteUuid) {
        this.remoteUuid = remoteUuid;
    }

    //--------------------------------------------------------------------------
    @ManyToOne
    private ChannelEntity channel = null;

    public ChannelEntity getChannel() {
        return channel;
    }

    public void setChannel(ChannelEntity channel) {
        this.channel = channel;
    }

    //--------------------------------------------------------------------------
    @Column
    private boolean recorded = false;

    public boolean isRecorded() {
        return recorded;
    }

    public void setRecorded(boolean recorded) {
        this.recorded = recorded;
    }

    //--------------------------------------------------------------------------
    @Column
    private boolean loaded = false;

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
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

    //--------------------------------------------------------------------------
    @Column
    private String extension = null;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    //--------------------------------------------------------------------------
    /**
     * Size in bytes.
     */
    @Column(name = "_size")
    private int size = 0;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    //--------------------------------------------------------------------------
    /**
     * Milliseconds.
     */
    @Column(name = "_begin")
    private Long begin = null;

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    //--------------------------------------------------------------------------
    /**
     * Milliseconds.
     */
    @Column(name = "_end")
    private Long end = null;

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    //--------------------------------------------------------------------------
    /**
     * Milliseconds.
     *
     * @return
     */
    public int getDuration() {
        return (int) (getEnd() - getBegin());
    }

    //--------------------------------------------------------------------------
    @Column
    private String samplerInfo;

    public String getSamplerInfo() {
        return samplerInfo;
    }

    public void setSamplerInfo(String samplerInfo) {
        this.samplerInfo = samplerInfo;
    }

    //--------------------------------------------------------------------------
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }
}
