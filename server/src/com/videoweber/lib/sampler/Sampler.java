package com.videoweber.lib.sampler;

import com.videoweber.lib.common.Executor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Sampler extends Executor {

    private static final Logger LOG = Logger.getLogger(Sampler.class.getName());
    public static final String VERSION = "1.0";
    private volatile SampleHandler sampleHandler;
    private Date lastSampleEnd;
    private volatile SamplerEngine samplerEngine = null;
    private final SampleFactory sampleFactory;
    private final Map<File, Date> rawSamples = new ConcurrentHashMap<>();
    private volatile boolean sampleEngineIsExecuting;

    public Sampler(SamplerEngine samplerEngine, SampleFactory sampleFactory) {
        Objects.requireNonNull(samplerEngine);
        Objects.requireNonNull(sampleFactory);

        samplerEngine.initRawSampleHandler((file, date) -> {
            Objects.requireNonNull(file);
            Objects.requireNonNull(date);
            synchronized (rawSamples) {
                rawSamples.put(file, date);
                rawSamples.notify();

            }
        });

        samplerEngine.getListeners().add(new Listener() {
            @Override
            public void onStart(Executor executor) {
                synchronized (rawSamples) {
                    sampleEngineIsExecuting = true;
                    rawSamples.notify();
                }
            }

            @Override
            public void onStop(Executor executor) {
                synchronized (rawSamples) {
                    sampleEngineIsExecuting = false;
                    rawSamples.notify();
                }
            }

            @Override
            public void onCrash(Executor executor) {
                synchronized (rawSamples) {
                    sampleEngineIsExecuting = false;
                    rawSamples.notify();
                }
            }
        });

        this.samplerEngine = samplerEngine;
        this.sampleFactory = sampleFactory;

        getListeners().add(new Listener() {
            @Override
            public void onStoping(Executor executor) {
                synchronized (rawSamples) {
                    rawSamples.notify();
                }
            }

        });
    }

    @Override
    public void run() {
        try {
            LOG.log(Level.FINE, "Attempt to start sampler.");

            /**
             * Assume that engine is started. Anyway this flag will be false in
             * future on engine stop.
             */
            sampleEngineIsExecuting = true;
            samplerEngine.start();

            while (!isStoping()) {
                LOG.log(Level.FINER, "Import samples files.");
                /**
                 *
                 */
                if (!sampleEngineIsExecuting) {
                    throw new RuntimeException("Unexpected stop of sampler engine.");
                }

                synchronized (rawSamples) {
                    if (rawSamples.isEmpty() && sampleEngineIsExecuting && !isStoping()) {
                        try {
                            rawSamples.wait();
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                handleRawSamples();
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Sampler is stopped on error.", e);
        } finally {
            if (samplerEngine != null) {
                samplerEngine.stopAndWaitForInfinitely();
            }
            handleRawSamples();
            LOG.log(Level.FINER, "Sampler is stopped.");
        }
    }

    public SampleHandler getSampleHandler() {
        return sampleHandler;
    }

    public void setSampleHandler(SampleHandler sampleHandler) {
        this.sampleHandler = sampleHandler;
    }

    private void handleRawSamples() {
        rawSamples.forEach((File file, Date date) -> {
            LOG.log(Level.FINEST, "Attempt to import sample file {0}", file.getName());
            if (lastSampleEnd != null) {
                long shift = Math.abs(date.getTime() - lastSampleEnd.getTime());
                if (shift < 10000) {
                    // Shift is not big, all is fine.
                    date = lastSampleEnd;
                } else {
                    // TODO: process large shift. And what if summer/winter time change occurs.
                    LOG.log(Level.WARNING, "Shift is larger than 10 seconds.");
                }
            }

            Sample sample;
            try {
                sample = sampleFactory.createSample(this, date, file);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't create sample.", e);
                rawSamples.remove(file);
                try {
                    Files.delete(file.toPath());
                } catch (IOException | RuntimeException deleFileException) {
                    LOG.log(Level.WARNING, String.format("Can't delete corrupted raw sample file^ \"%s\".", file.getAbsolutePath()), deleFileException);
                }
                return;
            }

            lastSampleEnd = new Date(sample.getEnd().getTime());

            if (sampleHandler != null) {
                try {
                    sampleHandler.onSample(sample);
                } catch (RuntimeException e) {
                    LOG.log(Level.WARNING, "Error in sampleHandler.", e);
                }
            }
            rawSamples.remove(file);
        });
    }

    /**
     * Sample's structure depends on sampler version. Sample's quality depends
     * on engine.
     *
     * @return {sampler version}/{engine name}_{engine version} or {sampler
     * version}
     */
    public String getInfo() {
        return VERSION + "/" + samplerEngine.getInfo();
    }

}
