package com.videoweber.client.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class FrameEntity implements Serializable {

    public FrameEntity() {
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
    @Column(name = "_date")
    private Long date = null;

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
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
