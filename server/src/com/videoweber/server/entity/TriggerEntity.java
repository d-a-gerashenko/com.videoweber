package com.videoweber.server.entity;

import java.io.Serializable;
import javax.persistence.*;
import com.videoweber.lib.common.HasMediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public abstract class TriggerEntity implements Serializable, HasMediaType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //--------------------------------------------------------------------------
    @Column
    private long durationBefore = 0;

    public long getDurationBefore() {
        return durationBefore;
    }

    public void setDurationBefore(long durationBefore) {
        this.durationBefore = durationBefore;
    }

    //--------------------------------------------------------------------------
    @Column
    private long durationAfter = 0;

    public long getDurationAfter() {
        return durationAfter;
    }

    public void setDurationAfter(long durationAfter) {
        this.durationAfter = durationAfter;
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
}
