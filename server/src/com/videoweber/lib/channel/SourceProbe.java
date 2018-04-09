package com.videoweber.lib.channel;

import com.videoweber.lib.common.MediaType;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class SourceProbe {

    private final Source source;
    private boolean inited = false;
    protected MediaType mediaType = null;

    protected SourceProbe(Source source) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
    }

    public void init() {
        if (!inited) {
            initSourceInfo();
        }
        inited = true;
    }

    public boolean isInited() {
        return inited;
    }

    protected abstract void initSourceInfo();

    public Source getSource() {
        return source;
    }

    public MediaType getMediaType() {
        if (!inited) {
            init();
        }
        return mediaType;
    }

    /**
     * @return If source MediaType is equal to MediaType from probe - true, else
     * - false.
     */
    public boolean isMediaTypesEqual() {
        return getSource().getMediaType() == getMediaType();
    }

    public boolean isMediaTypesCompatible() {
        return getSource().getMediaType().isCompatible(getMediaType());
    }
}
