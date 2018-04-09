package com.videoweber.lib.engines.javacv;

import com.videoweber.lib.cli.ContinuousRuntimeExec;
import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.engines.ffmpeg_exec.FfmpegBins;
import com.videoweber.lib.framer.Frame;
import com.videoweber.lib.framer.Framer;
import com.videoweber.lib.sampler.Sample;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegFramer extends Framer {

    private static final Logger LOG = Logger.getLogger(FfmpegFramer.class.getName());
    
    

    public FfmpegFramer(File tempDir) {
        super(tempDir);
    }

    @Override
    protected Frame _cutFrame(Sample sample) {
        File frameImage = new File(
                getTempDir().getAbsolutePath()
                + File.separator
                + FileNameFunstions.randomName()
                + ".jpg"
        );

        FFmpegFrameGrabber frameGrabber = null;
        try {
            frameGrabber = new FFmpegFrameGrabber(sample.getFile());
            OpenCVFrameConverter frameConverter = new OpenCVFrameConverter.ToIplImage();
            frameGrabber.start();
            opencv_imgcodecs.cvSaveImage(frameImage.getAbsolutePath(), frameConverter.convertToIplImage(frameGrabber.grabImage()));
        } catch (Exception ex) {
            throw new RuntimeException("Error on export jpg from sample.", ex);
        } finally {
            try {
                if (frameGrabber != null) {
                    frameGrabber.stop();
                    frameGrabber.release();
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in FfmpegFramer on grabber stop/release.", e);
            }
        }

        if (!frameImage.exists()) {
            throw new RuntimeException("Image file not found.");
        }

        return new Frame(sample.getBegin(), frameImage);
    }

}
