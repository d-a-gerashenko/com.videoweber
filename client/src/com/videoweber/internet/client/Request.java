package com.videoweber.internet.client;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class Request {

    private final String command;
    private final Object data;

    public Request(String command) {
        this(command, null);
    }

    public Request(String command, Object data) {
        if (command == null) {
            throw new NullPointerException();
        }
        if (command.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.command = command;
        this.data = data;
    }

    public final String getCommand() {
        return command;
    }

    public final Object getData() {
        return data;
    }
}
