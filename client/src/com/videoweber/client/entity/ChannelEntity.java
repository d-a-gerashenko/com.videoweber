package com.videoweber.client.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.UUID;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class ChannelEntity implements Serializable {

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
    @Column(length = 500)
    private String path = "";

    /**
     * @return / - separator
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path / - separator
     */
    public void setPath(String path) {
        this.path = path;
    }

}
