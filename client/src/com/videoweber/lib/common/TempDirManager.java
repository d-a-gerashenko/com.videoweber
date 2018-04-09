package com.videoweber.lib.common;

import java.io.File;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TempDirManager {

    private final String PREFIX = "tmp_";
    private final File rootDir;

    public TempDirManager(File rootDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException();
        }
        if (!rootDir.exists()) {
            throw new RuntimeException("TempDirManager root dir doesn't exist.");
        }
        File[] oldFiles = rootDir.listFiles(
                file -> file.getName().startsWith(PREFIX)
        );
        for (File oldFile : oldFiles) {
            deleteFolder(oldFile);
        }
        this.rootDir = rootDir;
    }

    public File getRootDir() {
        return rootDir;
    }

    public File createDir() {
        File newDir = new File(rootDir + File.separator + PREFIX + FileNameFunstions.randomName());
        newDir.mkdir();
        return newDir;
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
