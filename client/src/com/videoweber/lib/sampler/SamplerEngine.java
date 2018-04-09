package com.videoweber.lib.sampler;

import com.videoweber.lib.common.Executor;
import com.videoweber.lib.channel.Channel;
import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class SamplerEngine extends Executor {

    private static final Logger LOG = Logger.getLogger(SamplerEngine.class.getName());

    private final Channel channel;
    private final File tempDir;
    private BiConsumer<File, Date> rawSampleHandler = null;
    private final Object rawSampleHandlerSyncLock = new Object();

    public SamplerEngine(Channel channel, File tempDir) {
        Objects.requireNonNull(channel);
        Objects.requireNonNull(tempDir);

        if (!tempDir.exists()) {
            LOG.log(Level.SEVERE, "Sampler engine temp dir doesn't exist.");
            throw new RuntimeException("Sampler temp dir doesn't exist.");
        }

        this.channel = channel;
        this.tempDir = tempDir;
    }

    public Channel getChannel() {
        return channel;
    }

    public File getTempDir() {
        return tempDir;
    }

    public void initRawSampleHandler(BiConsumer<File, Date> rawSampleHandler) {
        synchronized (rawSampleHandlerSyncLock) {
            if (this.rawSampleHandler != null) {
                throw new IllegalStateException("rawSampleHandler already initialized.");
            }
            this.rawSampleHandler = rawSampleHandler;
        }
    }

    public BiConsumer<File, Date> getRawSampleHandler() {
        synchronized (rawSampleHandlerSyncLock) {
            if (rawSampleHandler == null) {
                throw new IllegalStateException("rawSampleHandler isn't initialized.");
            }
            return rawSampleHandler;
        }
    }

    /**
     * @return [a-z\-]
     */
    @Override
    public abstract String getName();

    /**
     * @return [a-z\-0-9\.]
     */
    public abstract String getVersion();

    /**
     * @return {engine name}_{engine version}
     */
    public String getInfo() {
        return getName() + "_" + getVersion();
    }
}
