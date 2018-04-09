package com.videoweber.internet.client.channel_request;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChFuture {

    private static final Logger LOG = Logger.getLogger(ChFuture.class.getName());

    private final ChRequest chRequest;
    private final Consumer<ChResponse> chResponseHandler;
    private ChResponse chResponse = null;
    private final FutureTask<ChResponse> futureTask;

    public ChFuture(ChRequest chRequest, Consumer<ChResponse> chResponseHandler) {
        if (chRequest == null) {
            throw new NullPointerException();
        }
        this.chRequest = chRequest;
        this.chResponseHandler = chResponseHandler;
        this.futureTask = new FutureTask<>(() -> chResponse);
    }

    public ChFuture(ChRequest chRequest) {
        this(chRequest, null);
    }

    public ChRequest getChRequest() {
        return chRequest;
    }

    public synchronized void setChResponse(ChResponse chResponse) {
        if (this.chResponse != null) {
            throw new IllegalStateException("Channel response already set.");
        }
        if (chResponse == null) {
            throw new NullPointerException();
        }
        this.chResponse = chResponse;
        if (chResponseHandler != null) {
            try {
                chResponseHandler.accept(chResponse);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, String.format("Response handler was executed with errors for request type %s.", chRequest.getCommand()), e);
            }
        }
        futureTask.run();
    }

    public ChResponse getChResponse() throws InterruptedException, CancellationException {
        try {
            return futureTask.get();
        } catch (ExecutionException e) {
            // Ignoring. There is no ExecutionException in uor case.
            throw new RuntimeException(e);
        }
    }

    public boolean isDone() {
        return futureTask.isDone();
    }

    public boolean isCancelled() {
        return futureTask.isCancelled();
    }

    public boolean cancel() {
        /**
         * There is no matter true or false because there is only one operation
         * to execute - return.
         */
        return futureTask.cancel(false);
    }

}
