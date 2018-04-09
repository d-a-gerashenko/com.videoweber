package com.videoweber.client.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.hibernate.Session;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SampleRepository extends Repository<SampleEntity> {

    public SampleRepository(AbstractHibernateService abstractHibernateService, Class<SampleEntity> entityClass) {
        super(abstractHibernateService, entityClass);
    }

    public SampleEntity get(ChannelEntity channelEntity, Date begin) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (SampleEntity) session
                    .createQuery("SELECT se FROM SampleEntity se WHERE channel = :channel AND begin = :begin AND deleted IS NULL")
                    .setParameter("channel", channelEntity)
                    .setLong("begin", begin.getTime())
                    .uniqueResult();
        });
    }

    public long getCountByRange(ChannelEntity channelEntity, Date begin, Date end) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (long) session.createQuery(""
                    + "SELECT COUNT(*) "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.channel = :channel "
                    + "AND "
                    + "("
                    + "se.begin >= :begin AND se.begin <= :end OR "
                    + "se.end >= :begin AND se.end <= :end OR "
                    + "se.begin < :begin AND se.end > :end "
                    + ")")
                    .setParameter("channel", channelEntity)
                    .setParameter("begin", begin.getTime())
                    .setParameter("end", end.getTime())
                    .uniqueResult();
        });
    }

    public long getCountByDay(ChannelEntity channelEntity, Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date begin = new Date(cal.getTimeInMillis());

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date end = new Date(cal.getTimeInMillis());

        return getCountByRange(channelEntity, begin, end);
    }

    public List<SampleEntity> findByRange(ChannelEntity channelEntity, Date begin, Date end) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.channel = :channel "
                    + "AND "
                    + "("
                    + "se.begin >= :begin AND se.begin <= :end OR "
                    + "se.end >= :begin AND se.end <= :end OR "
                    + "se.begin < :begin AND se.end > :end "
                    + ")"
                    + "ORDER BY "
                    + "se.begin ASC ")
                    .setParameter("channel", channelEntity)
                    .setParameter("begin", begin.getTime())
                    .setParameter("end", end.getTime())
                    .list();
        });
    }

    public List<SampleEntity> findUnloadedByRange(ChannelEntity channelEntity, Date begin, Date end) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.loaded = :loaded "
                    + "AND "
                    + "se.channel = :channel "
                    + "AND "
                    + "("
                    + "se.begin >= :begin AND se.begin <= :end OR "
                    + "se.end >= :begin AND se.end <= :end OR "
                    + "se.begin < :begin AND se.end > :end "
                    + ")"
                    + "ORDER BY "
                    + "se.begin ASC ")
                    .setParameter("loaded", false)
                    .setParameter("channel", channelEntity)
                    .setParameter("begin", begin.getTime())
                    .setParameter("end", end.getTime())
                    .list();
        });
    }

    public Long getDownloadSizeByRange(ChannelEntity channelEntity, Date begin, Date end) {
        return getAbstractHibernateService().acquireSession((session) -> {
            Long size = (Long) session.createQuery(""
                    + "SELECT SUM(se.size) "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.loaded = :loaded "
                    + "AND "
                    + "se.channel = :channel "
                    + "AND "
                    + "("
                    + "se.begin >= :begin AND se.begin <= :end OR "
                    + "se.end >= :begin AND se.end <= :end OR "
                    + "se.begin < :begin AND se.end > :end "
                    + ")")
                    .setParameter("loaded", false)
                    .setParameter("channel", channelEntity)
                    .setParameter("begin", begin.getTime())
                    .setParameter("end", end.getTime())
                    .uniqueResult();
            return (size == null) ? 0 : size;
        });
    }

    public SampleEntity findFirstByChannel(ChannelEntity channelEntity) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (SampleEntity) session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.channel = :channel "
                    + "ORDER BY "
                    + "se.begin ASC "
                    + "")
                    .setMaxResults(1)
                    .setParameter("channel", channelEntity)
                    .uniqueResult();
        });
    }

    public SampleEntity findLastByChannel(ChannelEntity channelEntity) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (SampleEntity) session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.channel = :channel "
                    + "ORDER BY "
                    + "se.begin DESC "
                    + "")
                    .setMaxResults(1)
                    .setParameter("channel", channelEntity)
                    .uniqueResult();
        });
    }

    public SampleEntity findLastLoadedByChannel(ChannelEntity channelEntity) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (SampleEntity) session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.loaded = :loaded "
                    + "AND "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.channel = :channel "
                    + "ORDER BY "
                    + "se.begin DESC "
                    + "")
                    .setMaxResults(1)
                    .setParameter("loaded", true)
                    .setParameter("channel", channelEntity)
                    .uniqueResult();
        });
    }

    public void markDeleted(SampleEntity sampleEntity) {
        sampleEntity.setDeleted(new Date());
        sampleEntity.setChannel(null);
        getAbstractHibernateService().update(sampleEntity);
    }

    public List<SampleEntity> findOldDeleted() {
        return getAbstractHibernateService().acquireSession((session) -> {
            Date deletedLimit = new Date(new Date().getTime() - 10 * 60 * 1000); // 10 minutes
            return session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NOT NULL "
                    + "AND "
                    + "se.deleted <= :deletedLimit "
                    + "ORDER BY "
                    + "se.begin ASC ")
                    .setParameter("deletedLimit", deletedLimit)
                    .setMaxResults(30)
                    .list();
        });
    }

    public void deleteByChannel(ChannelEntity channelEntity) {
        getAbstractHibernateService().acquireSession((session) -> {
            session.createQuery(""
                    + "UPDATE SampleEntity se "
                    + "SET se.deleted = :now"
                    + ", "
                    + "se.channel = NULL "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.channel = :channel "
                    + "")
                    .setParameter("now", new Date())
                    .setParameter("channel", channelEntity)
                    .executeUpdate();
        });
    }

    public void deleteUnloadedBefore(SampleEntity sampleEntity) {
        getAbstractHibernateService().acquireSession((session) -> {
            session.createQuery(""
                    + "DELETE FROM SampleEntity se "
                    + "WHERE "
                    + "se.channel = :channel "
                    + "AND "
                    + "se.loaded = :loaded "
                    + "AND "
                    + "se.end <= :limit "
                    + "")
                    .setParameter("channel", sampleEntity.getChannel())
                    .setParameter("loaded", false)
                    .setParameter("limit", sampleEntity.getBegin())
                    .executeUpdate();
        });
    }

    public List<SampleEntity> findUnrecorded(ChannelEntity channelEntity, int limit) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.channel = :channel "
                    + "AND "
                    + "se.loaded = :loaded "
                    + "ORDER BY "
                    + "se.begin ASC "
                    + "")
                    .setParameter("channel", channelEntity)
                    .setParameter("loaded", false)
                    .setMaxResults(limit)
                    .list();
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
                    + "UPDATE SampleEntity se "
                    + "SET se.deleted = :now "
                    + ", "
                    + "se.channel = NULL "
                    + "WHERE "
                    + "se.deleted IS NULL "
                    + "AND "
                    + "se.end <= :limit "
                    + "")
                    .setParameter("now", new Date())
                    .setParameter("limit", limit)
                    .executeUpdate();
        });
    }

    public SampleEntity findNextLoadedSample(ChannelEntity channelEntity, Date after) {
        return getAbstractHibernateService().acquireSession((session) -> {
            return (SampleEntity) session.createQuery(""
                    + "SELECT se "
                    + "FROM SampleEntity se "
                    + "WHERE se.end > :after "
                    + "AND se.loaded = :loaded "
                    + "AND se.channel = :channel "
                    + "AND deleted IS NULL "
                    + "ORDER BY se.begin "
                    + "ASC"
                    + "")
                    .setParameter("loaded", true)
                    .setParameter("channel", channelEntity)
                    .setLong("after", after.getTime())
                    .setMaxResults(1)
                    .uniqueResult();
        });
    }

}
