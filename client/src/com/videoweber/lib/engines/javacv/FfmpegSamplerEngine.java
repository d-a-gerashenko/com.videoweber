package com.videoweber.lib.engines.javacv;

import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.channel.Channel;
import com.videoweber.lib.channel.Source;
import com.videoweber.lib.channel.sources.Rtsp;
import com.videoweber.lib.engines.javacv.streaming_chunker.StreamingChunker;
import com.videoweber.lib.engines.javacv.streaming_chunker.simplified_grabber.FFmpegAudioWrap;
import com.videoweber.lib.engines.javacv.streaming_chunker.simplified_grabber.FFmpegMixerWrap;
import com.videoweber.lib.engines.javacv.streaming_chunker.simplified_grabber.FFmpegVideoWrap;
import com.videoweber.lib.engines.javacv.streaming_chunker.simplified_grabber.FpsReductionGrabber;
import com.videoweber.lib.engines.javacv.streaming_chunker.simplified_grabber.SimplifiedGrabber;
import com.videoweber.lib.engines.javacv.streaming_chunker.simplified_grabber.SimplifiedGrabberWrap;
import com.videoweber.lib.sampler.SamplerEngine;
import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacv.FFmpegFrameGrabber;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegSamplerEngine extends SamplerEngine {

    private static final Logger LOG = Logger.getLogger(FfmpegSamplerEngine.class.getName());

    public static int FPS = 4;

    /**
     * Sample duration in microseconds.
     */
    private final static int SAMPLE_SIZE = 30000000;
    /**
     * In microseconds.
     */
    private final static int TIMEOUT = 3000000;
    private final StreamingChunker chunker;
    private ExecutorService executorService = null;

    public FfmpegSamplerEngine(Channel channel, File tempDir) {
        super(channel, tempDir);

        boolean sameSource = false;
        if (channel.getVideoSource() != null && channel.getAudioSource() != null
                && getFfmpegUri(channel.getAudioSource()).equals(getFfmpegUri(channel.getVideoSource()))) {
            sameSource = true;
        }

        Supplier<SimplifiedGrabber> supplier;

        if (sameSource) {
            supplier = () -> {
                return new FpsReductionGrabber(new SimplifiedGrabberWrap(getFfmpegGrabber(getFfmpegUri(channel.getVideoSource()))), FPS);
            };
        } else {
            if (channel.getVideoSource() != null && channel.getAudioSource() != null) {
                supplier = () -> {
                    return new FpsReductionGrabber(new FFmpegMixerWrap(
                            getFfmpegGrabber(getFfmpegUri(channel.getVideoSource())),
                            getFfmpegGrabber(getFfmpegUri(channel.getAudioSource()))
                    ), FPS);
                };
            } else {
                if (channel.getVideoSource() != null) {
                    supplier = () -> {
                        return new FpsReductionGrabber(new FFmpegVideoWrap(getFfmpegGrabber(getFfmpegUri(channel.getVideoSource()))), FPS);
                    };
                } else {
                    supplier = () -> {
                        return new FFmpegAudioWrap(getFfmpegGrabber(getFfmpegUri(channel.getVideoSource())));
                    };
                }
            }
        }

        chunker = new StreamingChunker(supplier, tempDir, SAMPLE_SIZE);
        chunker.setChunkHandler((chunkFile) -> {
            executorService.submit(() -> {
                getRawSampleHandler().accept(chunkFile, new Date(Long.valueOf(FileNameFunstions.withoutExtension(chunkFile.getName())) / 1000));
            });
        });
    }

    @Override
    public void run() {
        try {
            LOG.log(Level.FINER, "Attempt to start sampler engine \"{0}\".", getInfo());
            executorService = Executors.newSingleThreadExecutor();
            chunker.start();
            while (!isStoping()) {
                if (!chunker.next()) {
                    throw new RuntimeException("Chunker returned NULL.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Engine is stopped on error.", e);
        } finally {
            try {
                chunker.stop();
            } catch (Exception exception) {
                LOG.log(Level.WARNING, "Can't stop chunker.", exception);
            }
            executorService.shutdown();
            executorService = null;
            LOG.log(Level.FINER, "Engine is stopped.");
        }

    }

    private String getFfmpegUri(Source source) {
        if (source instanceof Rtsp) {
            return ((Rtsp) source).getUri();
        }
        throw new RuntimeException("Unsupported source device.");
    }

    private FFmpegFrameGrabber getFfmpegGrabber(String source) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(source);
        grabber.setOption("stimeout", String.valueOf(TIMEOUT));
        return grabber;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getName() {
        return "javacv-ffmpeg";
    }

}
