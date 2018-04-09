package com.videoweber.client.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.FrameEntity;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.hibernate.Session;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FrameRepository extends Repository<FrameEntity> {

    public FrameRepository(AbstractHibernateService abstractHibernateService, Class<FrameEntity> entityClass) {
        super(abstractHibernateService, entityClass);
    }

    public FrameEntity get(ChannelEntity channelEntity, Date date) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (FrameEntity) session
                    .createQuery("SELECT fe FROM FrameEntity fe WHERE channel = :channel AND date = :date AND deleted IS NULL")
                    .setParameter("channel", channelEntity)
                    .setLong("date", date.getTime())
                    .uniqueResult();
        });
    }

    public FrameEntity findFirstByChannel(ChannelEntity channelEntity) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (FrameEntity) session.createQuery(""
                    + "SELECT fe "
                    + "FROM FrameEntity fe "
                    + "WHERE "
                    + "fe.deleted IS NULL "
                    + "AND "
                    + "fe.channel = :channel "
                    + "ORDER BY "
                    + "se.date ASC "
                    + "")
                    .setMaxResults(1)
                    .setParameter("channel", channelEntity)
                    .uniqueResult();
        });
    }

    public FrameEntity findLastByChannel(ChannelEntity channelEntity) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (FrameEntity) session.createQuery(""
                    + "SELECT fe "
                    + "FROM FrameEntity fe "
                    + "WHERE "
                    + "fe.deleted IS NULL "
                    + "AND "
                    + "fe.channel = :channel "
                    + "ORDER BY "
                    + "fe.date DESC "
                    + "")
                    .setMaxResults(1)
                    .setParameter("channel", channelEntity)
                    .uniqueResult();
        });
    }

    public void markDeleted(FrameEntity farameEntity) {
        farameEntity.setDeleted(new Date());
        farameEntity.setChannel(null);
        getAbstractHibernateService().update(farameEntity);
    }

    public List<FrameEntity> findOldDeleted() {
        return getAbstractHibernateService().acquireSession((session) -> {
            Date deletedLimit = new Date(new Date().getTime() - 10 * 60 * 1000); // 10 minutes
            return session.createQuery(""
                    + "SELECT fe "
                    + "FROM FrameEntity fe "
                    + "WHERE "
                    + "fe.deleted IS NOT NULL "
                    + "AND "
                    + "fe.deleted <= :deletedLimit "
                    + "")
                    .setParameter("deletedLimit", deletedLimit)
                    .setMaxResults(30)
                    .list();
        });
    }

    public void deleteByChannel(ChannelEntity channelEntity) {
        getAbstractHibernateService().acquireSession((session) -> {
            session.createQuery(""
                    + "UPDATE FrameEntity fe "
                    + "SET fe.deleted = :now"
                    + ", "
                    + "fe.channel = NULL "
                    + "WHERE "
                    + "fe.deleted IS NULL "
                    + "AND "
                    + "fe.channel = :channel "
                    + "")
                    .setParameter("now", new Date())
                    .setParameter("channel", channelEntity)
                    .executeUpdate();
        });
    }

    /**
     * @param lifeTimeLimit In milliseconds.
     */
    public void markOldAsDeleted(long lifeTimeLimit) {
        if (lifeTimeLimit <= 0) {
            throw new IllegalArgumentException();
        }
        getAbstractHibernateService().acquireSession((session) -> {
            Date now = new Date();
            long limit = now.getTime() - lifeTimeLimit;
            session.createQuery(""
                    + "UPDATE FrameEntity fe "
                    + "SET fe.deleted = :now "
                    + ", "
                    + "fe.channel = NULL "
                    + "WHERE "
                    + "fe.deleted IS NULL "
                    + "AND "
                    + "fe.date <= :limit "
                    + "")
                    .setParameter("now", new Date())
                    .setParameter("limit", limit)
                    .executeUpdate();
        });
    }

}
