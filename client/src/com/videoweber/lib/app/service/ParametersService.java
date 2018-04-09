package com.videoweber.lib.app.service;

import com.videoweber.lib.app.App;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ParametersService extends Service {

    private final Properties properties = new Properties();

    public ParametersService(ServiceContainer serviceContainer) {
        super(serviceContainer);
        loadConfig(App.workingDir() + File.separator + "parameters.conf");
    }

    public synchronized final void loadConfig(String configPath) {
        try (FileInputStream inputStream = new FileInputStream(configPath)) {
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Can't load \"%s\"", configPath), e);
        }
    }

    public synchronized final void saveConfig(String configPath) {
        try (FileOutputStream outputStream = new FileOutputStream(configPath)) {
            properties.store(outputStream, "---videoweber.com---");
        } catch (Exception e) {
            throw new RuntimeException(String.format("Can't save \"%s\"", configPath), e);
        }
    }

    public String getPropertyOrNull(String key) {
        String property = properties.getProperty(key);
        return property;
    }

    public String getProperty(String key) {
        String property = properties.getProperty(key);
        if (property == null) {
            throw new RuntimeException("Can't get property with key \"" + key + "\".");
        }
        return property;
    }

    public String getProperty(String key, String defaultValue) {
        String property = properties.getProperty(key);
        if (property == null) {
            return defaultValue;
        }
        return property;
    }

}
