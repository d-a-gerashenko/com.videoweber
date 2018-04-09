package com.videoweber.server.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class StorageEntity implements Serializable {

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
    @Column(name = "_path")
    private String path = null;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    //--------------------------------------------------------------------------
    @Column(name = "_size")
    /**
     * Size in bytes.
     */
    private long size = 0;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    //--------------------------------------------------------------------------
    @Column
    private long sizeUsed = 0;

    public long getSizeUsed() {
        return sizeUsed;
    }

    public void setSizeUsed(long sizeUsed) {
        this.sizeUsed = sizeUsed;
    }

    public long getSizeFree() {
        return getSize() - getSizeUsed();
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
}
