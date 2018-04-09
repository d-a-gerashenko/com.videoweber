package com.videoweber.lib.engines.javacv;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.sampler.Probe;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegProbe extends Probe {

    private static final Logger LOG = Logger.getLogger(FfmpegProbe.class.getName());

    public FfmpegProbe(File mediaFile) {
        super(mediaFile);
    }

    @Override
    protected void initFileInfo() {
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(getMediaFile());
            grabber.start();
            duration = grabber.getLengthInTime() / 1000;
            boolean video = grabber.getVideoCodec() != 0;
            boolean audio = grabber.getAudioCodec() != 0;
            if (video && audio) {
                mediaType = MediaType.VIDEO_AND_AUDIO;
            } else {
                if (video) {
                    mediaType = MediaType.VIDEO;
                } else if (audio) {
                    mediaType = MediaType.AUDIO;
                } else {
                    throw new RuntimeException("Ffmpeg probe has not determined media tipe.");
                }
            }
        } catch (RuntimeException | FrameGrabber.Exception e) {
            throw new RuntimeException("Error during probbing.", e);
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in FfmpegProbe on grabber stop/release.", e);
            }
        }
    }

}
