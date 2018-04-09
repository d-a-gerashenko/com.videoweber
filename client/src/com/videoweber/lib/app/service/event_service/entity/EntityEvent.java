package com.videoweber.lib.app.service.event_service.entity;

import com.videoweber.lib.app.service.event_service.Event;
import java.io.Serializable;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EntityEvent extends Event {

    private final Class<?> entityClass;
    private final EntityOperation entityOperation;
    private final Serializable entityId;

    public EntityEvent(
            Class<?> entityClass,
            EntityOperation entityOperation,
            Serializable entityId
    ) {
        if (entityClass == null
                || entityOperation == null
                || entityId == null) {
            throw new NullPointerException();
        }
        this.entityClass = entityClass;
        this.entityOperation = entityOperation;
        this.entityId = entityId;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public EntityOperation getEntityOperation() {
        return entityOperation;
    }

    public Serializable getEntityId() {
        return entityId;
    }

}
