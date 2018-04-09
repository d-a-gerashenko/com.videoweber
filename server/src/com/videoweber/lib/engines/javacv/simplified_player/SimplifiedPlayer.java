package com.videoweber.lib.engines.javacv.simplified_player;

import com.videoweber.lib.track.player.SignedBufferedImage;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SimplifiedPlayer {

    private final String source;
    private final SoundBufferPlayer soundBufferPlayer;
    private SimplifiedPlayerTools tools = null;
    private SignedBufferedImage firstImage = null;
    private long firstRecordTimestamp = 0;
    private SignedBufferedImage lastImage = null;
    private SignedBufferedImage lastFrameImage = null;
    /**
     * In microseconds.
     */
    private long recordTimestamp = 0;
    private long lastNextTimestamp = 0;

    public SimplifiedPlayer(String source, SoundBufferPlayer soundBufferPlayer) throws SimplifiedPlayerException {
        Objects.requireNonNull(source);
        this.source = source;
        this.soundBufferPlayer = soundBufferPlayer;
        try {
            tools = new SimplifiedPlayerTools(source);
            Frame frame = tools.getGrabber().grab();
            if (frame == null) {
                throw new RuntimeException("No frames in source: " + source);
            }
            firstRecordTimestamp = tools.getGrabber().getTimestamp();
            if (frame.image == null) {
                tools.getGrabber().grabImage();
            }
            if (frame.image != null) {
                firstImage = SignedBufferedImage.copy(tools.getPaintConverter().convert(frame), firstImage);
            }
        } catch (FrameGrabber.Exception | LineUnavailableException ex) {
            throw new SimplifiedPlayerException(ex);
        }
        stop();
    }

    public final String getSource() {
        return source;
    }

    public final SoundBufferPlayer getSoundBufferPlayer() {
        return soundBufferPlayer;
    }

    /**
     * @return Last not null image. If last image is null returns first image.
     */
    public final SignedBufferedImage getCurrentImage() {
        return (lastImage != null) ? lastImage : firstImage;
    }

    /**
     * Last not null image.
     *
     * @return
     */
    public final BufferedImage getLastImage() {
        return lastImage;
    }

    public final BufferedImage getLastFrameImage() {
        return lastFrameImage;
    }

    public final long getTimestamp() {
        return recordTimestamp - firstRecordTimestamp;
    }

    /**
     * @return false - on end of data.
     * @throws SimplifiedPlayerException
     */
    public final boolean next() throws SimplifiedPlayerException {
        try {
            if (tools == null) {
                tools = new SimplifiedPlayerTools(source);
            }
            Frame frame = tools.getGrabber().grab();
            if (frame == null) {
                return false;
            }

            lastFrameImage = null;
            if (frame.image != null) {
                lastImage = SignedBufferedImage.copy(tools.getPaintConverter().convert(frame), lastImage);
                lastFrameImage = lastImage;
                if (tools.getGrabber().getAudioChannels() == 0 || soundBufferPlayer == null) {
                    /**
                     * Pause to control play speed. Frames from files changes
                     * too fast without sound playing. There is no such problem
                     * in streaming because stream controls play speed.
                     */
                    long timeAfterLastNext = System.nanoTime() / 1000 - lastNextTimestamp;
                    long timeAfterLastFrame = tools.getGrabber().getTimestamp() - recordTimestamp;
                    long timeToPauseBeforeNextFrame = timeAfterLastFrame - timeAfterLastNext;
                    if (timeToPauseBeforeNextFrame > 0) {
                        try {
                            TimeUnit.MICROSECONDS.sleep(timeToPauseBeforeNextFrame);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    lastNextTimestamp = System.nanoTime() / 1000;
                }
            } else {
                if (soundBufferPlayer != null) {
                    try {
                        soundBufferPlayer.play(tools.convertAudioBuffer(frame.samples[0]));
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            recordTimestamp = tools.getGrabber().getTimestamp();
            return true;
        } catch (FrameGrabber.Exception | LineUnavailableException | ExecutionException ex) {
            throw new SimplifiedPlayerException(ex);
        }
    }

    public final void seek(long seekTimestamp) throws SimplifiedPlayerException {
        stop();
        try {
            if (seekTimestamp < 0) {
                throw new SimplifiedPlayerException("Can't seek to less than zero.");
            }
            tools = new SimplifiedPlayerTools(source);
            if (seekTimestamp == 0) {
                return;
            }
            Frame frame;
            while ((frame = tools.getGrabber().grab()) != null
                    && getTimestamp() < seekTimestamp
                    && !Thread.currentThread().isInterrupted()) {
                lastFrameImage = null;
                if (frame.image != null) {
                    lastImage = SignedBufferedImage.copy(tools.getPaintConverter().convert(frame), lastImage);
                    lastFrameImage = lastImage;
                }
                recordTimestamp = tools.getGrabber().getTimestamp();
            }
        } catch (FrameGrabber.Exception | LineUnavailableException ex) {
            throw new SimplifiedPlayerException(ex);
        }
    }

    public final void stop() throws SimplifiedPlayerException {
        lastImage = null;
        lastFrameImage = null;
        recordTimestamp = 0;
        lastNextTimestamp = 0;
        if (Objects.nonNull(tools)) {
            try {
                tools.release();
            } catch (Exception ex) {
                throw new SimplifiedPlayerException(ex);
            }
            tools = null;
        }
    }

}
