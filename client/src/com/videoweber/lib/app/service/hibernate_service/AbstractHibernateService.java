package com.videoweber.lib.app.service.hibernate_service;

import com.videoweber.lib.app.service.hibernate_service.session_provider.SessionProvider;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.hibernate_service.session_provider.SessionAcquirement;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class AbstractHibernateService extends Service {

    private final SessionFactory sessionFactory;
    private final HashMap<Class, Repository> repositories = new HashMap<>();
    private final ThreadLocal<SessionProvider> sessionProviderHolder;

    public AbstractHibernateService(ServiceContainer serviceContainer, ConfigurationFactory configurationFactory) {
        super(serviceContainer);
        if (configurationFactory == null) {
            throw new NullPointerException();
        }
        sessionFactory = configurationFactory
                .createConfiguration()
                .buildSessionFactory();
        sessionProviderHolder = ThreadLocal.withInitial(() -> {
            return new SessionProvider(() -> {
                return sessionFactory.openSession();
            });
        });
    }

    public final void acquireSession(Consumer<Session> task) {
        SessionAcquirement sessionAcquirement = sessionProviderHolder.get().acquireSession();
        try {
            task.accept(sessionAcquirement.getSession());
        } finally {
            sessionAcquirement.release();
        }
    }

    public final <R> R acquireSession(Function<Session, R> task) {
        SessionAcquirement sessionAcquirement = sessionProviderHolder.get().acquireSession();
        try {
            return task.apply(sessionAcquirement.getSession());
        } finally {
            sessionAcquirement.release();
        }
    }

    public void save(Object entity) {
        acquireSession((session) -> {
            session.save(entity);
            session.flush();
        });
    }

    public void update(Object entity) {
        acquireSession((session) -> {
            session.update(entity);
            session.flush();
        });
    }

    public void delete(Object entity) {
        acquireSession((session) -> {
            session.delete(entity);
            session.flush();
        });
    }

    public void refresh(Object entity) {
        acquireSession((session) -> {
            session.refresh(entity);
        });
    }

    public synchronized <T> Repository<T> getRepository(Class<T> entityClass) {
        if (repositories.containsKey(entityClass)) {
            return (Repository<T>) repositories.get(entityClass);
        }
        Class<? extends Repository> repositoryClass = getRepositoryClass(entityClass);
        Repository newRepository;
        if (repositoryClass == null) {
            newRepository = new Repository(this, entityClass);
        } else {
            try {
                newRepository = repositoryClass
                        .getConstructor(AbstractHibernateService.class, Class.class)
                        .newInstance(this, entityClass);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("Inappropriate constructor of repository class.", ex);
            }

        }
        repositories.put(repositoryClass, newRepository);
        return (Repository<T>) newRepository;
    }

    private static Class<? extends Repository> getRepositoryClass(Class entityClass) {
        String className = entityClass.getSimpleName();
        int classNamePostfixIndex = className.lastIndexOf("Entity");
        if (classNamePostfixIndex > 0) {
            className = className.substring(0, classNamePostfixIndex) + "Repository";
        }

        String packageName = entityClass.getPackage().getName();
        int packageNamePostfixIndex = packageName.lastIndexOf("entity");
        if (packageNamePostfixIndex > 0) {
            packageName = packageName.substring(0, packageNamePostfixIndex) + "repository";
        }

        Class repositoryClass;
        try {
            repositoryClass = Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
        return repositoryClass;
    }
    
    public synchronized void release() {
        sessionFactory.close();
    }
}
