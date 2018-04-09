package com.videoweber.lib.JavaFX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class JavaFxSynchronous {

    private static enum State {
        NOT_STARTED, STARTED, DISPOSED;
    }
    private static State STATE = State.NOT_STARTED;
    private final static List<Task<Void>> TASKS = Collections.synchronizedList(new ArrayList<>());

    public static void safeRunLaterAndWaitForInfinitely(Runnable runnable) {
        Objects.requireNonNull(runnable);

        final Task<Void> task;
        synchronized (TASKS) {
            if (comareAndUpdateState(State.NOT_STARTED)) {
                Runtime.getRuntime().addShutdownHook(new Thread(JavaFxSynchronous::onAppStop));
            }
            if (comareAndUpdateState(State.DISPOSED)) {
                throw new RuntimeException("JavaFxSynchronous is disposed.");
            }

            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        TASKS.remove(this);
                    }
                    return null;
                }
            };
            TASKS.add(task);
        }

        /**
         * This method shouldn't be invoked in synchronized block because it
         * could run task that uses safeRunLaterAndWaitForInfinitely(). In this
         * case this task will be waiting for end of task which will be waiting
         * for end of synchronization lock created by this task.
         */
        JavaFxPlatform.safeRunLater(task);
        boolean interrupted = false;
        while (!task.isDone()) {
            try {
                task.get();
            } catch (InterruptedException ex) {
                Logger.getLogger(JavaFxSynchronous.class.getName()).log(Level.FINER, "Can't stop. Waiting for JavaFX task.", ex);
                interrupted = true;
            } catch (ExecutionException ex) {
                throw new RuntimeException("Exception during execution JavaFX task.", ex);
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * It should be called from shutdown hook and from onAppStop method of
     * JavaFX application. It couldn't be used only in one of them. Without
     * shutdown hook app will be waiting for task execution after System.exit()
     * and without invocation in stop() in the JavaFX app the shutdown hook will
     * never be reached because JavaFX doesn't exit from app until there are
     * some other running threads.
     *
     * One of the possible solutions is to call System.exit(0) from JavaFX app
     * stop() method.
     */
    public static void onAppStop() {
        synchronized (TASKS) {
            if (comareAndUpdateState(State.STARTED)) {
                TASKS.forEach((task) -> {
                    task.cancel();
                });
            }
        }
    }

    private static boolean comareAndUpdateState(State state) {
        Objects.requireNonNull(state);
        if (STATE == state) {
            switch (STATE) {
                case NOT_STARTED:
                    STATE = State.STARTED;
                    break;
                case STARTED:
                    STATE = State.DISPOSED;
                    break;
            }
            return true;
        }
        return false;
    }
}
