package com.videoweber.lib.framer;

import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface FramedTrackInterface {
    /**
     * @param position
     * @return Frame at position or first frame after position. Null - if there
     * is no more frames.
     */
    public Frame getFrame(Date position);

    /**
     *
     * @return Last frame from track or null.
     */
    public Frame getLastFrame();
}
