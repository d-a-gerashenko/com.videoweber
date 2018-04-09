package com.videoweber.client.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.client.entity.ChannelEntity;
import java.util.List;

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
            return session.createQuery(""
                    + "SELECT ce "
                    + "FROM ChannelEntity ce "
                    + "ORDER BY "
                    + "ce.path ASC, "
                    + "ce.order ASC "
                    + ")")
                    .list();
        });
    }

}
