package com.videoweber.lib.framer;

import java.io.File;
import java.util.Date;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Frame {

    private final Date date;
    private final File file;

    public Frame(Date date, File file) {
        if (date == null
                || file == null) {
            throw new IllegalArgumentException();
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File doesn't exist.");
        }
        this.date = date;
        this.file = file;
    }

    public Date getDate() {
        return date;
    }

    public File getFile() {
        return file;
    }

}
