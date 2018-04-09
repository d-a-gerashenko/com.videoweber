package com.videoweber.lib.app.service.hibernate_service;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 * @param <T> Entity class.
 */
public class Repository<T> {

    private final AbstractHibernateService abstractHibernateService;
    private final Class<T> entityClass;

    public Repository(AbstractHibernateService abstractHibernateService, Class<T> entityClass) {
        this.abstractHibernateService = abstractHibernateService;
        this.entityClass = entityClass;
    }

    public AbstractHibernateService getAbstractHibernateService() {
        return abstractHibernateService;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public T get(Serializable id) {
        return abstractHibernateService.acquireSession((session) -> {
            return (T) session.get(entityClass, id);
        });
    }

    public List<T> findAll() {
        return abstractHibernateService.acquireSession((session) -> {
            return session.createCriteria(entityClass).list();
        });
    }
}
