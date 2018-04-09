package com.videoweber.lib.app.service.hibernate_service;

import com.videoweber.lib.common.ResourceManager;
import java.io.File;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ConfigurationFactory {

    public Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        File configFile = ResourceManager.getResourceFile("hibernate.xml");
        if (!configFile.canRead()) {
            throw new RuntimeException("Can't read hibernate config file from: " + configFile.getAbsolutePath());
        }
        configuration.configure(configFile);
        return configuration;
    }
}
