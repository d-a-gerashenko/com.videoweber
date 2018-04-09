package com.videoweber.lib.cli;

import com.videoweber.lib.common.Executor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ContinuousRuntimeExec {

    private static final Logger LOG = Logger.getLogger(ContinuousRuntimeExec.class.getName());

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final String command;
    private Process process = null;
    private ContinuousStreamReader errorReader = null;
    private ContinuousStreamReader outputReader = null;
    private volatile Consumer<String> onOutputEventHandler;
    private volatile Consumer<String> onErrorEventHandler;

    public ContinuousRuntimeExec(String command) {
        this(command, new Object[0]);
    }

    public ContinuousRuntimeExec(String command, Object[] args) {
        if (command == null || args == null) {
            throw new IllegalArgumentException();
        }
        if (args.length > 0) {
            this.command = String.format(command, args);
        } else {
            this.command = command;
        }
    }

    public void exec() throws IOException {
        readWriteLock.writeLock().lock();
        try {
            LOG.log(Level.FINE, "Attempt to start command execution \"{0}\".", this.command);
            if (isExecuting()) {
                LOG.log(Level.WARNING, "Command execution is already started.");
                return;
            }

            try {
                process = Runtime.getRuntime().exec(this.command);
            } catch (IOException ex) {
                LOG.log(Level.FINER, "Error during command execution \"" + this.command + "\".", ex);
                throw ex;
            }

            LOG.log(Level.FINER, "Adding output reader.");
            outputReader = new ContinuousStreamReader(process.getInputStream(), onOutputEventHandler);
            outputReader.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
                readWriteLock.readLock().lock();
                try {
                    if (!outputReader.equals(t)) {
                        /**
                         * Ignoring previous ContinuousStreamReader exceptions
                         * if ContinuousRuntimeExec was restarted.
                         */
                        return;
                    }
                    LOG.log(Level.SEVERE, "Command execution \"{0}\" is interrupted due to termination of output reader thread.", this.command);
                    stop();
                } finally {
                    readWriteLock.readLock().unlock();
                }
            });
            outputReader.start();

            LOG.log(Level.FINER, "Adding error reader.");
            errorReader = new ContinuousStreamReader(process.getErrorStream(), onErrorEventHandler);
            errorReader.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
                readWriteLock.readLock().lock();
                try {
                    if (!errorReader.equals(t)) {
                        /**
                         * Ignoring previous ContinuousStreamReader exceptions
                         * if ContinuousRuntimeExec was restarted.
                         */
                        return;
                    }
                    LOG.log(Level.SEVERE, "Command execution \"{0}\" is interrupted due to termination of error reader thread.", this.command);
                    stop();
                } finally {
                    readWriteLock.readLock().unlock();
                }
            });
            errorReader.start();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void stop() {
        readWriteLock.readLock().lock();
        try {
            LOG.log(Level.FINER, "Attempt to destroy command process.");
            if (!isExecuting()) {
                LOG.log(Level.WARNING, "Command is already stopped.");
                return;
            }
            process.destroy();
            LOG.log(Level.FINER, "Command process destroyed.");
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

    public void stopAndWaitForInfinitely() {
        readWriteLock.readLock().lock();
        try {
            stop();
            waitForInfinitely();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void waitForInfinitely() {
        readWriteLock.readLock().lock();
        try {
            LOG.log(Level.FINER, "Waiting for end of command execution.");

            /**
             * Without this var loop will always show log warning after
             * Thread.currentThread() interrupt. Remember that thread status
             * resets after InterruptedException and it needs to restore it.
             */
            boolean interrupted = false;
            while (isExecuting()) {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    LOG
                            .log(
                                    Level.WARNING,
                                    String.format("Can't stop. Waiting process infinitely \"%s\".", command),
                                    e
                            );
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }

            LOG.log(Level.FINER, "Command execution is finished.");
            LOG.log(Level.FINER, "Waiting for end of readers.");

            Executor.waitForInfinitely(outputReader);
            Executor.waitForInfinitely(errorReader);
            LOG.log(Level.FINER, "Readers are stopped.");
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public String getCommand() {
        return command;
    }

    public boolean isExecuting() {
        readWriteLock.readLock().lock();
        try {
            return process != null && process.isAlive();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public ContinuousStreamReader getErrorReader() {
        readWriteLock.readLock().lock();
        try {
            return errorReader;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public ContinuousStreamReader getOutputReader() {
        readWriteLock.readLock().lock();
        try {
            return outputReader;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public BufferedWriter getInput() {
        readWriteLock.readLock().lock();
        try {
            return new BufferedWriter(
                    new OutputStreamWriter(
                            process.getOutputStream()
                    )
            );
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Consumer<String> getOnOutput() {
        return onOutputEventHandler;
    }

    public void setOnOutput(Consumer<String> onOutput) {
        this.onOutputEventHandler = onOutput;
    }

    public Consumer<String> getOnError() {
        return onErrorEventHandler;
    }

    public void setOnError(Consumer<String> onError) {
        this.onErrorEventHandler = onError;
    }

}
