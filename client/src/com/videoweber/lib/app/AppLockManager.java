package com.videoweber.lib.app;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class AppLockManager {

    private static final Logger LOG = Logger.getLogger(AppLockManager.class.getName());

    public static enum AppLockMode {
        SESSION(0), PATH(1), NAME(2);

        private final int level;

        private AppLockMode(int lavel) {
            this.level = lavel;
        }

        public int getLevel() {
            return level;
        }
    }

    public static abstract class Messages {

        public static final String SHUT_DOWN_APP = "SHUT_DOWN_APP";
        public static final String GET_APP_SESSION_UID = "GET_APP_SESSION_UID";
        public static final String GET_APP_PATH = "GET_APP_PATH";
        public static final String GET_APP_NAME = "GET_APP_NAME";
        public static final String GET_APP_VERSION = "GET_APP_VERSION";
        public static final String GET_APP_INFO = "GET_APP_INFO";
        public static final String GET_LOCK_MODE = "GET_LOCK_MODE";
    }

    private static final MessageHandler DEFAULT_MESSAGE_HANDLER = (String message) -> {
        switch (message) {
            case Messages.SHUT_DOWN_APP:
                App.exit();
                return null; // It's ok. JUnique.sendMessage will retern "".
            case Messages.GET_APP_SESSION_UID:
                return App.sessionUid();
            case Messages.GET_APP_PATH:
                return App.path().getAbsolutePath();
            case Messages.GET_APP_NAME:
                return App.name();
            case Messages.GET_APP_VERSION:
                return App.version();
        }

        String appInfo = String.format(
                "name: \"%s\", path: \"%s\", version: \"%s\", session: \"%s\"",
                App.name(),
                App.path(),
                App.version(),
                App.sessionUid()
        );

        if (message.equals(Messages.GET_APP_INFO)) {
            return appInfo;
        }

        return String.format(
                "Unsupported message \"%s\" got by app (%s).",
                message,
                appInfo
        );
    };

    private AppLockManager() {
    }

    private static MessageHandler createMessageHandler(AppMessageHandler appMessageHandler, AppLockMode appLockMode) {
        return (String message) -> {
            message = message.toUpperCase();
            String result;
            try {
                result = appMessageHandler.handle(message);
            } catch (UnsupportedAppMessageException unsupportedAppMessageException) {
                if (message.equals(Messages.GET_LOCK_MODE)) {
                    result = appLockMode.name();
                } else {
                    result = DEFAULT_MESSAGE_HANDLER.handle(message);
                }
            }
            return result;
        };
    }

    private static String uniquePath(File file) {
        String uniquePath;
        try {
            uniquePath = file.getCanonicalPath();
        } catch (IOException ex) {
            LOG.log(
                    Level.WARNING,
                    "Can't get canonical path. Trying to get absolute path it could cause to incorrect result.",
                    ex
            );
            uniquePath = file.getAbsolutePath();
        }
        return uniquePath;
    }

    public static String sendMessage(String appSessionUid, String message) throws UndeliveredMessageException {
        if (message == null) {
            throw new NullPointerException();
        }
        if (!isLocked(appSessionUid)) {
            throw new UndeliveredMessageException(
                    String.format("There is no lock for appSessionUid \"%s\"", appSessionUid)
            );
        }
        String result = JUnique.sendMessage(appSessionUid, message);
        if (result == null) {
            throw new UndeliveredMessageException(
                    String.format("There is lock for appSessionUid \"%s\". But result is null.", appSessionUid)
            );
        }
        return result;
    }

    public static boolean isLocked(String appSessionUid) {
        return JUnique.sendMessage(appSessionUid, Messages.GET_APP_INFO) != null;
    }

    public static AppLockMode lockMode(String appSessionUid) throws UndeliveredMessageException {
        return AppLockMode.valueOf(sendMessage(appSessionUid, Messages.GET_LOCK_MODE));
    }

    public static void acquireAppLock(AppLockMode appLockMode) throws AlreadyLockedException {
        acquireAppLock(appLockMode, (String message) -> {
            throw new UnsupportedAppMessageException();
        });
    }

    public static void acquireAppLock(AppLockMode appLockMode, AppMessageHandler appMessageHandler) throws AlreadyLockedException {
        if (appLockMode == null || appMessageHandler == null) {
            throw new NullPointerException();
        }
        App app = App.instance();
        MessageHandler messageHandler = createMessageHandler(appMessageHandler, appLockMode);
        String appUniquePath = uniquePath(app.getPath());
        try {
            JUnique.acquireLock(app.getSessionUid(), messageHandler);

            if (appLockMode.getLevel() >= 1) {
                JUnique.acquireLock(appUniquePath, messageHandler);
            }
            if (appLockMode.getLevel() == 2) {
                JUnique.acquireLock(app.getName(), messageHandler);
            }
        } catch (AlreadyLockedException alreadyLockedException) {
            JUnique.releaseLock(app.getSessionUid());
            JUnique.releaseLock(appUniquePath);
            JUnique.releaseLock(app.getName());
            throw alreadyLockedException;
        }
    }

    public static void releaseAppLock() {
        App app = App.instance();
        String appUniquePath = uniquePath(app.getPath());

        JUnique.releaseLock(app.getSessionUid());
        JUnique.releaseLock(appUniquePath);
        JUnique.releaseLock(app.getName());
    }

    public static String sessionUidByPath(File appPath) throws UndeliveredMessageException {
        return sendMessage(uniquePath(appPath), Messages.GET_APP_SESSION_UID);
    }

    public static String sessionUidByName(String appName) throws UndeliveredMessageException {
        return sendMessage(appName, Messages.GET_APP_SESSION_UID);
    }

}
