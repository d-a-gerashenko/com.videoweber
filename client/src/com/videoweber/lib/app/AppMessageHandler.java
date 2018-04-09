package com.videoweber.lib.app;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface AppMessageHandler {
    public String handle(String message) throws UnsupportedAppMessageException;
}
