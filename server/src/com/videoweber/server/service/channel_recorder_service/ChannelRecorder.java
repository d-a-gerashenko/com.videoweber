package com.videoweber.server.service.channel_recorder_service;

import com.videoweber.lib.common.Executor;
import com.videoweber.lib.sampler.Sampler;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelRecorder {

    private static final Logger LOG = Logger.getLogger(ChannelRecorder.class.getName());

    /**
     * In milliseconds.
     */
    private final static long RESTARTING_TIMEOUT = 10000;

    private final ReentrantLock syncLock = new ReentrantLock();
    private Sampler sampler;
    private volatile boolean samplerCrashListenerEnabled = true;
    private final Supplier<Sampler> samplerSupplier;
    private boolean started = false;
    private long restartScheduled = -1;
    private Thread restartThread = null;

    public ChannelRecorder(Supplier<Sampler> samplerSupplier) {
        Objects.requireNonNull(samplerSupplier);
        this.samplerSupplier = samplerSupplier;
        refresh();
    }

    public void start() {
        syncLock.lock();
        try {
            if (started == true) {
                return;
            }
            /**
             * На тот случай, когда в канала нет данных, он пустой. Он как бы
             * запущен, но не работает.
             */
            if (sampler == null) {
                started = true;
                return;
            }
            cancelRestarting();
            started = true;
            sampler.start();
        } finally {
            syncLock.unlock();
        }
    }

    public void stop() {
        syncLock.lock();
        try {
            if (started == false) {
                return;
            }
            if (sampler == null) {
                started = false;
                return;
            }
            cancelRestarting();
            started = false;
            sampler.stop();
            samplerCrashListenerEnabled = false;
            sampler.waitForInfinitely();
            samplerCrashListenerEnabled = true;
        } finally {
            syncLock.unlock();
        }
    }

    public final void refresh() {
        syncLock.lock();
        try {
            Sampler newSampler = samplerSupplier.get();
            boolean wasStarted = started;
            stop();
            sampler = newSampler;
            if (sampler != null) {
                sampler.getListeners().add(new Executor.Listener() {
                    @Override
                    public void onCrash(Executor executor) {
                        LOG.log(Level.WARNING, "Sampler crashed.", executor.getLastCrashException());
                        /**
                         * samplerCrashListenerEnabled позволяет избежать
                         * срабатывания обработчика краша при ручной остановке.
                         * Даже если была ошибка и при ручной остановке опять
                         * будет ошибка, обработчик не будет нам уже мешать.
                         */
                        while (true) {
                            if (syncLock.tryLock()) {
                                try {
                                    scheduleRestarting();
                                    break;
                                } finally {
                                    syncLock.unlock();
                                }
                            }
                            if (!samplerCrashListenerEnabled) {
                                break;
                            }
                        }
                    }

                });
            }
            if (wasStarted) {
                start();
            }
        } finally {
            syncLock.unlock();
        }
    }

    public boolean isActive() {
        syncLock.lock();
        try {
            return sampler != null;
        } finally {
            syncLock.unlock();
        }
    }

    public boolean isStarted() {
        syncLock.lock();
        try {
            return started;
        } finally {
            syncLock.unlock();
        }
    }

    public boolean isRestarting() {
        syncLock.lock();
        try {
            return restartThread != null;
        } finally {
            syncLock.unlock();
        }
    }

    public long getTimeToRestart() {
        syncLock.lock();
        try {
            if (restartScheduled == -1) {
                return 0;
            }
            long timeLeft = TimeUnit.SECONDS.convert(
                    RESTARTING_TIMEOUT - (System.currentTimeMillis() - restartScheduled),
                    TimeUnit.MILLISECONDS
            );
            return (timeLeft < 0) ? 0 : timeLeft;
        } finally {
            syncLock.unlock();
        }
    }

    private void scheduleRestarting() {
        cancelRestarting();
        started = false;
        restartThread = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(RESTARTING_TIMEOUT);
            } catch (InterruptedException ex) {
                return;
            }
            syncLock.lock();
            try {
                if (!Thread.interrupted()) {
                    ChannelRecorder.this.start();
                }
            } finally {
                syncLock.unlock();
            }
        });
        restartThread.setName("ChannelRecorder restart thread.");
        restartThread.start();
        restartScheduled = System.currentTimeMillis();
    }

    private void cancelRestarting() {
        if (restartThread != null) {
            restartThread.interrupt();
            restartThread = null;
            restartScheduled = -1;
        }
    }

}
