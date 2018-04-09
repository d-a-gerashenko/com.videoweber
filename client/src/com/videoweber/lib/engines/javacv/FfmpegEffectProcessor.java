package com.videoweber.lib.engines.javacv;

import com.videoweber.lib.sampler.Effect;
import com.videoweber.lib.sampler.EffectProcessor;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.sampler.effects.Rotate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegEffectProcessor extends EffectProcessor {

    private static final Logger LOG = Logger.getLogger(FfmpegEffectProcessor.class.getName());

    private final File tempDir;

    public FfmpegEffectProcessor(File tempDir) {
        if (tempDir == null) {
            throw new IllegalArgumentException();
        }
        if (!tempDir.exists()) {
            throw new RuntimeException("Temp dit is not exists.");
        }
        this.tempDir = tempDir;
    }

    @Override
    public Sample applyEffect(Sample sample, Effect effect) {
        if (!effect.getMediaType().isCompatible(sample.getMediaType())) {
            throw new RuntimeException(
                    String.format("Sample media type is not campatible with effect media type %s.", effect.getClass())
            );
        }
        if (effect instanceof Rotate) {
            File tempFile = new File(
                    tempDir.getAbsolutePath()
                    + File.separator
                    + sample.getFile().getName()
            );
            String command = null;

            int angel = ((Rotate) effect).getAngel();
            switch (angel) {
                case 90:
                    command = "transpose=1";
                    break;
                case 180:
                    command = "transpose=1,transpose=1";
                    break;
                case 270:
                    command = "transpose=2";
                    break;
            }
            if (command != null) {
                FFmpegFrameGrabber grabber = null;
                FFmpegFrameFilter filter = null;
                FFmpegFrameRecorder recorder = null;
                try {
                    grabber = new FFmpegFrameGrabber(sample.getFile());
                    grabber.start();

                    filter = new FFmpegFrameFilter(command, grabber.getImageWidth(), grabber.getImageHeight());
                    filter.start();

                    if (angel == 90 || angel == 270) {
                        recorder = new FFmpegFrameRecorder(
                                tempFile,
                                grabber.getImageHeight(),
                                grabber.getImageWidth()
                        );
                    } else {
                        recorder = new FFmpegFrameRecorder(
                                tempFile,
                                grabber.getImageWidth(),
                                grabber.getImageHeight()
                        );
                    }
                    recorder.setFormat("mp4");
                    recorder.setAudioChannels(grabber.getAudioChannels());
                    recorder.setVideoCodec(grabber.getVideoCodec());
                    recorder.setVideoOption("preset", "ultrafast");
                    recorder.setVideoOption("crf", "22");
                    recorder.setSampleRate(grabber.getSampleRate());
                    recorder.setAudioCodec(grabber.getAudioCodec());
                    recorder.setFrameRate(grabber.getFrameRate());
                    recorder.start();

                    Frame frame;
                    while ((frame = grabber.grab()) != null) {
                        if (frame.image != null) {
                            filter.push(frame);
                            frame = filter.pull();
                        }
                        recorder.record(frame);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (grabber != null) {
                            grabber.stop();
                            grabber.release();
                        }
                        if (filter != null) {
                            filter.stop();
                            filter.release();
                        }
                        if (recorder != null) {
                            recorder.stop();
                            recorder.release();
                        }
                    } catch (Exception exception) {
                        LOG.log(Level.SEVERE, "Crash in ffmpeg filter.", exception);
                    }
                }
                LOG.log(Level.FINER, "Replase sample file with rotated file.");
                try {
                    Files.move(tempFile.toPath(), sample.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Cant't replase sample file with rotated.", e);
                }
            }

        } else {
            throw new RuntimeException("Unsupported effect: " + effect.getClass().getName());
        }
        return new Sample(
                sample.getSamplerInfo(),
                sample.getBegin(),
                sample.getExtension(),
                sample.getFile(),
                sample.getDudation(),
                (int) sample.getFile().length(),
                sample.getMediaType()
        );
    }

    @Override
    public boolean isSupportedEffect(Effect effect) {
        return effect instanceof Rotate;
    }

}
