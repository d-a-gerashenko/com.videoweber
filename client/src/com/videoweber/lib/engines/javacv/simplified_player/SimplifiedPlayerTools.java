package com.videoweber.lib.engines.javacv.simplified_player;

import com.videoweber.lib.track.player.SignedBufferedImage;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.LineUnavailableException;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SimplifiedPlayerTools {

    private static final double SC16 = (double) 0x7FFF + 0.4999999999999999;
    private final String source;
    private FFmpegFrameGrabber grabber = null;
    private Java2DFrameConverter paintConverter = null;
    private ExecutorService executor = null;
    private List<Exception> releaseExceptions = null;

    public SimplifiedPlayerTools(String source) throws FrameGrabber.Exception, LineUnavailableException {
        Objects.requireNonNull(source);
        this.source = source;
        grabber = new FFmpegFrameGrabber(source);
        grabber.start();
        paintConverter = new Java2DFrameConverter();
        executor = Executors.newSingleThreadExecutor();
    }

    void release() throws Exception {
        releaseExceptions = new ArrayList<>();
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (FrameGrabber.Exception ex) {
                releaseExceptions.add(ex);
            }
        }
        grabber = null;
        paintConverter = null;
        if (executor != null) {
            executor.shutdown();
        }
        executor = null;
        if (!releaseExceptions.isEmpty()) {
            throw new Exception(
                    String.format(
                            "Exceptions (count: %s) on tools release.",
                            releaseExceptions.size()
                    ),
                    releaseExceptions.get(0)
            );
        }
    }

    public byte[] convertAudioBuffer(Buffer buffer) {
        FloatBuffer channelSamplesFloatBuffer = (FloatBuffer) buffer;
        channelSamplesFloatBuffer.rewind();

        ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesFloatBuffer.capacity() * 2);
        for (int i = 0; i < channelSamplesFloatBuffer.capacity(); i++) {
            outBuffer.putShort((short) ((double) channelSamplesFloatBuffer.get(i) * SC16));
        }

        return outBuffer.array();
    }

    public List<Exception> getReleaseExceptions() {
        return Collections.unmodifiableList(releaseExceptions);
    }

    public String getSource() {
        return source;
    }

    public FFmpegFrameGrabber getGrabber() {
        return grabber;
    }

    public Java2DFrameConverter getPaintConverter() {
        return paintConverter;
    }

}
