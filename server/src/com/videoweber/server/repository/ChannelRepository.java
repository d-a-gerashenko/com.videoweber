package com.videoweber.server.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.server.entity.ChannelEntity;
import java.util.List;
import org.hibernate.criterion.Order;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChannelRepository extends Repository<ChannelEntity> {

    public ChannelRepository(AbstractHibernateService abstractHibernateService, Class<ChannelEntity> entityClass) {
        super(abstractHibernateService, entityClass);
    }

    @Override
    public List<ChannelEntity> findAll() {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session.createCriteria(ChannelEntity.class)
                    .addOrder(Order.asc("order"))
                    .list();
        });
    }
    
}
