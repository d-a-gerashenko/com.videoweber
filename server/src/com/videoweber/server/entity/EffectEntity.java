package com.videoweber.server.entity;

import java.io.Serializable;
import javax.persistence.*;
import com.videoweber.lib.common.HasMediaType;
import com.videoweber.lib.common.MediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class EffectEntity implements Serializable, HasMediaType {

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
    private ChannelEntity channel = null;

    public ChannelEntity getChannel() {
        return channel;
    }

    public void setChannel(ChannelEntity channel) {
        this.channel = channel;
    }

    //--------------------------------------------------------------------------
    @Override
    public MediaType getMediaType() {
        throw new UnsupportedOperationException("Not supported in this object.");
    }
}
