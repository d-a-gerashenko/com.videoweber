package com.videoweber.lib.app.service.widget_service;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface WidgetEventListener {

    public default void onWidgetInitialized() {
    }
    public default void onWidgetCall(Object[] parameters) {
    }
}
