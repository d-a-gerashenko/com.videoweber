package com.videoweber.internet.client.channel_request;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface ChRequestProcessor {

    public ChResponse process(ChRequest chRequest);

    public String getCommand();
}
