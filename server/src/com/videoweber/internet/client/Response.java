package com.videoweber.internet.client;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Response {

    public static enum Status {
        SUCCESS, ERROR;
    };
    private final Status status;
    private final Object data;

    public Response(Status status, Object data) {
        this.status = status;
        this.data = data;
    }

    public final Status getStatus() {
        return status;
    }

    public final Object getData() {
        return data;
    }

    
}
