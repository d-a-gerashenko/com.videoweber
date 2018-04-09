package com.videoweber.lib.app;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UndeliveredMessageException extends Exception {

    public UndeliveredMessageException(String message) {
        super(message);
    }
}
