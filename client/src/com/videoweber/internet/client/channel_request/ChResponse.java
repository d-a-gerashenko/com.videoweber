package com.videoweber.internet.client.channel_request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class ChResponse {

    public static enum Status {
        SUCCESS, ERROR;
    };
    private final Status status;
    private final Object data;

    public ChResponse(Status status, Object data) {
        if (status == null
                || data == null) {
            throw new NullPointerException();
        }
        this.status = status;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}
