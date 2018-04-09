package com.videoweber.lib.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class MultiFileLogHandler extends Handler {

    private final File rootDir;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-00");
    private final String extension = "txt";
    private String currentLogFilePath;
    private FileHandler currentFileHandler;
    private long lastClearOldTimeMillis = -1;

    public MultiFileLogHandler(File rootDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException();
        }
        rootDir.mkdirs();
        if (!rootDir.exists()) {
            throw new RuntimeException("Can't create log root dir.");
        }
        if (!rootDir.canWrite()) {
            throw new RuntimeException("Log root dir isn't writable.");
        }
        this.rootDir = rootDir;
    }

    private String generateLogFilePath() {
        Date date = new Date();
        return rootDir.getAbsolutePath() + File.separator
                + dateFormat.format(date);
    }

    private void clearOld() {
        if (lastClearOldTimeMillis != -1
                && System.currentTimeMillis() - lastClearOldTimeMillis <= 3600000 // 1 * 60 * 60 * 1000
                ) {
            return;
        }
        File[] oldFiles = rootDir.listFiles(
                file -> {
                    BasicFileAttributes attr;
                    try {
                        attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    } catch (IOException ex) {
                        throw new RuntimeException("Can't clear old log.", ex);
                    }
                    return System.currentTimeMillis() - attr.creationTime().toMillis() > 172800000; // 48 * 60 * 60 * 1000
                }
        );
        for (File oldFile : oldFiles) {
            oldFile.delete();
        }
        lastClearOldTimeMillis = System.currentTimeMillis();
    }

    @Override
    public synchronized void publish(LogRecord record) {
        clearOld();
        String generatedLogFilePath = generateLogFilePath();
        if (currentFileHandler == null
                || !currentLogFilePath.equals(generatedLogFilePath)) {
            if (currentFileHandler != null) {
                currentFileHandler.flush();
                currentFileHandler.close();
            }

            currentLogFilePath = generatedLogFilePath;
            File currentLogFile = new File(currentLogFilePath + "." + extension);
            currentLogFile.getParentFile().mkdirs();
            int i = 0;
            while (currentLogFile.exists()) {
                currentLogFile = new File(currentLogFilePath + "_" + ++i + "." + extension);
            }
            try {
                currentFileHandler = new FileHandler(currentLogFile.getPath());
                currentFileHandler.setFormatter(new SimpleFormatter());
            } catch (Exception ex) {
                throw new RuntimeException(String.format("Can't set file handler for logger: %s", currentLogFile.getAbsolutePath()), ex);
            }
        }
        currentFileHandler.publish(record);
    }

    @Override
    public synchronized void flush() {
        currentFileHandler.flush();
    }

    @Override
    public synchronized void close() throws SecurityException {
        currentFileHandler.close();
    }
}
