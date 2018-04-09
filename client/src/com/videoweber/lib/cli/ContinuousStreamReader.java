package com.videoweber.lib.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ContinuousStreamReader extends Thread {

    private static final Logger LOG = Logger.getLogger(ContinuousStreamReader.class.getName());

    private final List<String> lines = Collections.synchronizedList(new ArrayList<>());
    private final InputStream inputStream;
    private final Consumer<String> newLineHandler;

    public ContinuousStreamReader(InputStream inputStream, Consumer<String> newLineHandler) {
        Objects.requireNonNull(inputStream);

        this.inputStream = inputStream;
        this.newLineHandler = newLineHandler;
    }

    public ContinuousStreamReader(InputStream inputStream) {
        this(inputStream, null);
    }

    @Override
    public void run() {
        if (!Thread.currentThread().equals(this)) {
            throw new RuntimeException("ContinuousStreamReader run() couldn't be run from outside. Use start() instead.");
        }
        
        LOG.log(Level.FINER, "ContinuousStreamReader is started.");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
                if (newLineHandler != null) {
                    try {
                        newLineHandler.accept(line);
                    } catch (RuntimeException re) {
                        LOG.log(Level.WARNING, "Error in onLineEventHandler.", re);
                    }
                }
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Reader thread is stop due to IOException.", ioe);
            getUncaughtExceptionHandler().uncaughtException(this, ioe);
            return;
        }
        LOG.log(Level.FINER, "ContinuousStreamReader is stopped.");
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public String getLastLine() {
        synchronized (lines) {
            if (lines.size() > 0) {
                return lines.get(lines.size() - 1);
            } else {
                return null;
            }
        }
    }

    public String getMessage() {
        return getMessage(0);
    }

    /**
     *
     * @param count 0 - all lines.
     * @return
     */
    public String getMessage(int count) {
        synchronized (lines) {
            if (count == 0) {
                count = lines.size();
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                try {
                    stringBuilder.append(lines.get(i)).append(System.lineSeparator());
                } catch (Exception e) {
                    break;
                }
            }
            return stringBuilder.toString();
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Consumer<String> getNewLineHandler() {
        return newLineHandler;
    }

}
