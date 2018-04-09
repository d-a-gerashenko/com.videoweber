package com.videoweber.server.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.EffectEntity;
import java.util.List;
import org.hibernate.Session;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EffectRepository extends Repository<EffectEntity> {

    public EffectRepository(AbstractHibernateService abstractHibernateService, Class<EffectEntity> entityClass) {
        super(abstractHibernateService, entityClass);
    }

    public List<EffectEntity> findAllByChannel(ChannelEntity channelEntity) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session
                    .createQuery("SELECT ee FROM EffectEntity ee WHERE channel = :channel ORDER BY order")
                    .setParameter("channel", channelEntity)
                    .list();
        });
    }
    
    public void deleteByChannel(ChannelEntity channelEntity) {
        getAbstractHibernateService().acquireSession((session) -> {
            session.createQuery(""
                    + "DELETE EffectEntity ee "
                    + "WHERE "
                    + "ee.channel = :channel "
                    + "")
                    .setParameter("channel", channelEntity)
                    .executeUpdate();
        });
    }

}
