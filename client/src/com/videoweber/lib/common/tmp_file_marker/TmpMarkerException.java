package com.videoweber.lib.common.tmp_file_marker;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TmpMarkerException extends RuntimeException {

    public TmpMarkerException() {
    }

    public TmpMarkerException(String message) {
        super(message);
    }

    public TmpMarkerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TmpMarkerException(Throwable cause) {
        super(cause);
    }

}
