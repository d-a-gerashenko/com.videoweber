package com.videoweber.server.entity;

import javax.persistence.*;
import com.videoweber.lib.common.MediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
@Entity
public class EffectRotateEntity extends EffectEntity {

    @Column
    private int angel;

    public void setAngel(int angel) {
        switch (angel) {
            case 90:
                break;
            case 180:
                break;
            case 270:
                break;
            default:
                throw new IllegalArgumentException("Unsupported angel value.");
        }
        this.angel = angel;
    }

    public int getAngel() {
        return angel;
    }

    //--------------------------------------------------------------------------
    @Override
    public final MediaType getMediaType() {
        return MediaType.VIDEO;
    }
}
