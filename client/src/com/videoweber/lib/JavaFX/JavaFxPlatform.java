package com.videoweber.lib.JavaFX;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class JavaFxPlatform {

    private static final Logger LOG = Logger.getLogger(JavaFxPlatform.class.getName());
    
    public static void safeRunLater(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            LOG.log(Level.FINEST, "Current thread is not FxApplicationThread. The task has been scheduled.");
            Platform.runLater(runnable);
        }
    }
}
