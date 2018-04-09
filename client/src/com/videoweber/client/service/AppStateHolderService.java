package com.videoweber.client.service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.common.VarManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class AppStateHolderService extends Service {

    private final File stateFile;

    public AppStateHolderService(ServiceContainer serviceContainer) {
        super(serviceContainer);
        stateFile = new File(VarManager.getRootDir() + File.separator + "app_sate");
        try {
            if (stateFile.createNewFile()) {
                setState("new");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Can't create app state file: " + stateFile.getAbsolutePath(), ex);
        }
        if (!stateFile.canWrite()) {
            throw new RuntimeException("App state file isn't writable: " + stateFile.getAbsolutePath());
        }
    }

    public synchronized String getState() {
        try {
            return new String(Files.readAllBytes(stateFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Can't read data from app state file: " + stateFile.getAbsolutePath(), ex);
        }
    }

    public synchronized void setState(String state) {
        try {
            Files.write(stateFile.toPath(), state.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException("Can't write data to app state file: " + stateFile.getAbsolutePath(), ex);
        }
    }

}
