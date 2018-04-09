package com.videoweber.client.service;

import com.videoweber.lib.app.service.ParametersService;
import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.hibernate_service.ConfigurationFactory;
import com.videoweber.lib.common.VarManager;
import java.io.File;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class HibernateService extends AbstractHibernateService {

    public HibernateService(ServiceContainer serviceContainer) {
        super(serviceContainer, new ConfigurationFactory() {
            @Override
            public Configuration createConfiguration() {
                ParametersService parameters = serviceContainer
                        .getService(ParametersService.class);
                String dbName  = parameters.getProperty("DB_NAME");
                String dbUser  = parameters.getProperty("DB_USER");
                String dbPassword  = parameters.getProperty("DB_PASSWORD");
                return super.createConfiguration()
                    .setProperty(
                            "hibernate.connection.url",
                            "jdbc:h2:" + VarManager.getRootDir().getAbsolutePath() + File.separator + "database" + File.separator + dbName
                )
                .setProperty("hibernate.connection.username", dbUser)
                .setProperty("hibernate.connection.password", dbPassword)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.hbm2ddl.auto", "update")
                .setProperty("hibernate.connection.CharSet", "utf8")
                .setProperty("hibernate.connection.characterEncoding", "utf8")
                .setProperty("hibernate.connection.useUnicode", "true");
            }
            
        });
    }

}
