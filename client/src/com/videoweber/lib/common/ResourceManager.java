package com.videoweber.lib.common;

import com.videoweber.lib.app.App;
import java.io.File;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class ResourceManager {
    private final static File ROOT_DIR;

    static {
        ROOT_DIR = new File(App.workingDir() + File.separator + "resources");
        if (!ROOT_DIR.exists()) {
            throw new RuntimeException(String.format("ResourceManager root dit doesn't exist \"%s\".", ROOT_DIR));
        }
        if (!ROOT_DIR.canRead()) {
            throw new RuntimeException(String.format("ResourceManager root dit doesn't accessible \"%s\".", ROOT_DIR));
        }
    }

    public static File getRootDir() {
        return ROOT_DIR;
    }

    /**
     * @param path Example "com/videoweber/lib/app/tray_icon.png"
     * @return
     */
    public static File getResourceFile(String path) {
        File resourceFile = new File(getRootDir() + File.separator + path);
        if (!resourceFile.exists()) {
            throw new RuntimeException(
                    String.format("Resource file doesn't exist \"%s\".", resourceFile)
            );
        }
        return resourceFile;
    }
}
