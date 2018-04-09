package com.videoweber.lib.app.service;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gda
 */
public class SchedulerService extends Service {

    private static final Logger LOG = Logger.getLogger(SchedulerService.class.getName());
    private final Object sync = new Object();
    private int taskCounter = 0;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private CountDownLatch exitLatch = null;

    public SchedulerService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    /**
     * @param runnable
     * @param repeatDelay Seconds.
     */
    public void schedule(Runnable runnable, int repeatDelay) {
        synchronized (sync) {
            if (shutdownLatch.getCount() == 0) {
                throw new RuntimeException("SchedulerService is shutting down.");
            }
            taskCounter++;
        }
        Thread thread = new Thread(() -> {
            while (shutdownLatch.getCount() != 0) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error during scheduled task execution.", e);
                }
                synchronized (sync) {
                    if (shutdownLatch.getCount() != 0) {
                        try {
                            sync.wait(repeatDelay * 1000);
                        } catch (InterruptedException ex) {
                            LOG.severe("Unexpected interruption.");
                        }
                    }
                }
            }
            exitLatch.countDown();
        });
        thread.setName(thread.getName() + " / SchedulerService");
        thread.start();
    }

    public void release() {
        synchronized (sync) {
            shutdownLatch.countDown();
            sync.notifyAll();
            exitLatch = new CountDownLatch(taskCounter);
        }

        try {
            exitLatch.await();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Can't wait for scheduler stop.", ex);
        }
    }
}