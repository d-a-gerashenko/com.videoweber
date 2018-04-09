package com.videoweber.lib.app.service;

import com.videoweber.lib.common.TempDirManager;
import com.videoweber.lib.common.VarManager;
import java.io.File;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TempDirService extends Service {

    private final TempDirManager tempDirManager;

    public TempDirService(ServiceContainer serviceContainer) {
        super(serviceContainer);
        File rootDir = new File(VarManager.getRootDir() + File.separator + "tmp");
        rootDir.mkdirs();
        if (!rootDir.exists()) {
            throw new RuntimeException("Can't create TempDirService root dir.");
        }
        if (!rootDir.canWrite()) {
            throw new RuntimeException("TempDirService root dir isn't writable.");
        }
        tempDirManager = new TempDirManager(rootDir);
    }

    public File createTempDir() {
        return tempDirManager.createDir();
    }

    public File getRootDir() {
        return tempDirManager.getRootDir();
    }
}
