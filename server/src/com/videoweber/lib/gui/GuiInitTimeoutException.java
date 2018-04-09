package com.videoweber.lib.gui;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class GuiInitTimeoutException extends Exception {

    public GuiInitTimeoutException() {
    }

    public GuiInitTimeoutException(String message) {
        super(message);
    }

    public GuiInitTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuiInitTimeoutException(Throwable cause) {
        super(cause);
    }
    
}
