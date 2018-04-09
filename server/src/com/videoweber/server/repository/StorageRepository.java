package com.videoweber.server.repository;

import com.videoweber.lib.app.service.hibernate_service.AbstractHibernateService;
import com.videoweber.lib.app.service.hibernate_service.Repository;
import com.videoweber.server.entity.StorageEntity;
import java.util.List;
import org.hibernate.criterion.Order;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class StorageRepository extends Repository<StorageEntity> {

    public StorageRepository(AbstractHibernateService abstractHibernateService, Class entityClass) {
        super(abstractHibernateService, entityClass);
    }

    @Override
    public List<StorageEntity> findAll() {
        return getAbstractHibernateService().acquireSession((session) -> {
            return session.createCriteria(StorageEntity.class)
                    .addOrder(Order.asc("order"))
                    .list();
        });
    }

    public long getCommonFreeSize() {
        return findAll()
                .stream()
                .map((StorageEntity storageEntity) -> storageEntity.getSizeFree())
                .reduce(0L, (Long accumulator, Long storageFreeSize) -> accumulator + storageFreeSize);
    }

}
