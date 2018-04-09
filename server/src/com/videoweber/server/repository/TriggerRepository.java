package com.videoweber.server.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.TriggerEntity;
import java.util.List;
import org.hibernate.Session;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TriggerRepository extends Repository<TriggerEntity> {

    public TriggerRepository(AbstractHibernateService abstractHibernateService, Class<TriggerEntity> entityClass) {
        super(abstractHibernateService, entityClass);
    }

    public List<TriggerEntity> findAllByChannel(ChannelEntity channelEntity) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session
                    .createQuery("SELECT te FROM TriggerEntity te WHERE channel = :channel ORDER BY id")
                    .setParameter("channel", channelEntity)
                    .list();
        });
    }

    public void deleteByChannel(ChannelEntity channelEntity) {
        getAbstractHibernateService().acquireSession((session) -> {
            session.createQuery(""
                    + "DELETE TriggerEntity te "
                    + "WHERE "
                    + "te.channel = :channel "
                    + "")
                    .setParameter("channel", channelEntity)
                    .executeUpdate();
        });
    }

}
