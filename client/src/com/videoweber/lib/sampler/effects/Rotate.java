package com.videoweber.lib.sampler.effects;

import com.videoweber.lib.sampler.Effect;
import com.videoweber.lib.common.MediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Rotate extends Effect {

    private int angel;

    public Rotate(int angel) {
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

    @Override
    public final MediaType getMediaType() {
        return MediaType.VIDEO;
    }

}
