package com.videoweber.lib.app.service.window_service;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface WindowEventListener {

    public default void onWindowInitialized() {
    }
    public default void onWindowOpened(Object[] parameters) {
    }
}
