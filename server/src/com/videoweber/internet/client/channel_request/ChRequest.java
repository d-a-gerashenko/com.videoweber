package com.videoweber.internet.client.channel_request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class ChRequest {

    private final String channelUid;
    private final String command;
    private final Object data;

    public ChRequest(String channelUid, String command, Object data) {
        if (channelUid == null || command == null) {
            throw new NullPointerException();
        }
        if (channelUid.isEmpty() || command.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.channelUid = channelUid;
        this.command = command;
        this.data = data;
    }
    
    public ChRequest(String channelUid, String command) {
        this(channelUid, command, null);
    }

    public String getChannelUid() {
        return channelUid;
    }

    public String getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }

}
