package com.videoweber.lib.common.tmp_file_marker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TmpMarker {

    private final File file;

    public TmpMarker(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public TmpMarkerFlag createFlag() {
        TmpMarkerFlag flag = new TmpMarkerFlag();
        File flagFile = getFlagFile();
        try (FileOutputStream fos = new FileOutputStream(flagFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);) {
            oos.writeObject(flag);
        } catch (IOException ex) {
            throw new TmpMarkerException(String.format("Can't create possible tmp file flag: %s.", flagFile.getAbsolutePath()), ex);
        }
        return flag;
    }

    public void deleteFlag() {
        File flagFile = getFlagFile();
        try {
            Files.deleteIfExists(flagFile.toPath());
        } catch (IOException ex) {
            throw new TmpMarkerException(String.format("Can't delete possible tmp file flag: %s.", flagFile.getAbsolutePath()), ex);
        }
    }

    public TmpMarkerFlag readFlag() {
        TmpMarkerFlag flag = null;
        File flagFile = getFlagFile();
        try (FileInputStream fis = new FileInputStream(flagFile);
                ObjectInputStream ois = new ObjectInputStream(fis);) {

            flag = (TmpMarkerFlag) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            // Ignoring.
        }
        return flag;
    }

    private File getFlagFile() {
        return new File(file.getAbsolutePath() + TmpMarkers.FLAG_POSTFIX);
    }
}
