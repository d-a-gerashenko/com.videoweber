package com.videoweber.server.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.server.entity.SourceEntity;
import java.util.List;
import org.hibernate.criterion.Order;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SourceRepository extends Repository<SourceEntity> {

    public SourceRepository(AbstractHibernateService abstractHibernateService, Class<SourceEntity> entityClass) {
        super(abstractHibernateService, entityClass);
    }

    @Override
    public List<SourceEntity> findAll() {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session.createCriteria(SourceEntity.class)
                    .addOrder(Order.asc("title"))
                    .list();
        });
    }

}
