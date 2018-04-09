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
public class SourceEntity implements Serializable, HasMediaType {

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
    @Column(length = 50)
    private String title = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    //--------------------------------------------------------------------------
    @Override
    public MediaType getMediaType() {
        throw new UnsupportedOperationException("Not supported in this object.");
    }
}
