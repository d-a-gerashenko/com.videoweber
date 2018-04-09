package com.videoweber.lib.common;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class UncompatibleMediaTypeException extends RuntimeException {

    public UncompatibleMediaTypeException(String message, MediaType got, MediaType expected) {
        super(
                String.format(message + " Got %s. Expected compatible with %s.", got, expected)
        );
    }

}
