package com.videoweber.lib.engines.javacv.streaming_chunker.simplified_grabber;

import java.util.Objects;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FpsReductionGrabber implements SimplifiedGrabber {

    private final SimplifiedGrabber simplifiedGrabber;
    private long timestamp = 0;
    private final int fps;
    private final OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
    private opencv_core.Mat mat = null;

    public FpsReductionGrabber(SimplifiedGrabber simplifiedGrabber, int fps) {
        Objects.requireNonNull(simplifiedGrabber);
        if (fps < 1) {
            throw new IllegalArgumentException();
        }
        this.simplifiedGrabber = simplifiedGrabber;
        this.fps = fps;
    }

    @Override
    public int getAudioChannels() {
        return simplifiedGrabber.getAudioChannels();
    }

    @Override
    public int getImageWidth() {
        return simplifiedGrabber.getImageWidth();
    }

    @Override
    public int getImageHeight() {
        return simplifiedGrabber.getImageHeight();
    }

    @Override
    public void start() throws FrameGrabber.Exception {
        simplifiedGrabber.start();
    }

    @Override
    public void stop() throws FrameGrabber.Exception {
        simplifiedGrabber.stop();
    }

    @Override
    public Frame grab() throws FrameGrabber.Exception {
        Frame grabbedFrame;
        Frame outFrame;
        while (true) {
            grabbedFrame = simplifiedGrabber.grab();
            if (grabbedFrame == null) {
                return null;
            }
            if (grabbedFrame.image != null) {
                if (grabbedFrame.timestamp >= timestamp) {
                    if (mat != null) {
                        mat.release();
                    }
                    mat = matConverter.convert(grabbedFrame);
                    outFrame = matConverter.convert(mat);
                    outFrame.timestamp = timestamp;
                    outFrame.keyFrame = true;
                    timestamp += Math.round(1000000 / fps);
                } else {
                    continue;
                }
            } else {
                outFrame = grabbedFrame;
            }
            return outFrame;
        }

    }

    public int getFps() {
        return fps;
    }

}
