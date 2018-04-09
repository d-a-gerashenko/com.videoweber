package com.videoweber.lib.app.service.widget_service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class WidgetService extends Service {

    private final HashMap<Class<? extends Widget>, Widget> widgets = new HashMap<>();

    public WidgetService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public <T extends Widget> T getWidget(Class<T> widgetClass) {
        Objects.requireNonNull(widgetClass);
        
        if (!widgets.containsKey(widgetClass)) {
            throw new RuntimeException(String.format("Widgets \"%s\" isn't initialized yet.", widgetClass.getName()));
        }
        return (T) widgets.get(widgetClass);
    }

    private <T extends Widget> Widget createWidget(Class<T> widgetClass) {
        Objects.requireNonNull(widgetClass);
        
        if (widgets.containsKey(widgetClass)) {
            throw new RuntimeException(String.format("Widget \"%s\" is already initialized.", widgetClass.getName()));
        }

        Parent rootNode;
        WidgetController controller;
        try {
            FXMLLoader fXMLLoader = new FXMLLoader(
                    widgetClass.getResource(getFxmlFileName(widgetClass))
            );
            rootNode = fXMLLoader.load();
            controller = fXMLLoader.getController();
            if (controller != null) {
                controller.setServiceContainer(getServiceContainer());
            }
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Can't load FXML for widget \"%s\".", widgetClass.getName()),
                    ex
            );
        }

        Widget widget;
        try {
            widget = widgetClass.getConstructor(Parent.class, WidgetController.class).newInstance(rootNode, controller);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(String.format("Can't crate widget \"%s\".", widgetClass.getName()), ex);
        }

        return widget;
    }

    public <T extends Widget> T callWidget(Class<T> widgetClass, boolean isTemp, Object[] parameters) {
        Objects.requireNonNull(widgetClass);

        Widget widget;

        if (!isTemp) {
            if (!widgets.containsKey(widgetClass)) {
                widgets.put(widgetClass, createWidget(widgetClass));
            }
            widget = widgets.get(widgetClass);
        } else {
            widget = createWidget(widgetClass);
        }

        if (!widget.isInitialized()) {
            if (widget.getController() != null) {
                widget.getController().onWidgetInitialized();
            }
            widget.setInitialized();
        }
        if (widget.getController() != null) {
            widget.getController().onWidgetCall(parameters);
        }

        return (T) widget;
    }

    private static String getFxmlFileName(Class<? extends Widget> widgetClass) {
        String str = widgetClass.getSimpleName();
        int ind = str.lastIndexOf("Widget");
        if (ind > 0) {
            str = str.substring(0, ind);
        }
        return str + ".fxml";
    }
}
