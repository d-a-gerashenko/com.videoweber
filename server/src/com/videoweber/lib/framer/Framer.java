package com.videoweber.lib.framer;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.common.ResourceManager;
import com.videoweber.lib.sampler.Sample;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class Framer {

    private static final File DAFAULT_AUDIO_FRAME_IMAGE = ResourceManager.getResourceFile("com/videoweber/lib/player/audio_only.jpg");
    private final Map<String, Frame> cache = new HashMap<>();
    private final File tempDir;

    public Framer(File tempDir) {
        if (tempDir == null) {
            throw new IllegalArgumentException();
        }
        if (!tempDir.exists()) {
            throw new RuntimeException("Framer temp dir doesn't exist.");
        }
        this.tempDir = tempDir;
    }

    public File getTempDir() {
        return tempDir;
    }

    /**
     *
     * @param sample
     * @return
     */
    protected abstract Frame _cutFrame(Sample sample);

    /**
     *
     * @param sample
     * @return
     */
    public Frame cutFrame(Sample sample) {
        if (sample == null) {
            throw new NullPointerException("Sample is null.");
        }
        if (!sample.getMediaType().isCompatible(MediaType.VIDEO)) {
            return new Frame(sample.getBegin(), DAFAULT_AUDIO_FRAME_IMAGE);
        }
        String sampleFilePath = sample.getFile().getAbsolutePath();
        synchronized (cache) {
            if (cache.containsKey(sampleFilePath)) {
                return cache.get(sampleFilePath);
            }
            Frame newFrame = _cutFrame(sample);
            cache.put(sampleFilePath, newFrame);
            return newFrame;
        }
    }
}
