package com.videoweber.lib.app.service.event_service.entity;

import com.videoweber.lib.app.service.event_service.SpecifiedEventListener;
import java.io.Serializable;
import java.util.function.Consumer;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EntityEventListener extends SpecifiedEventListener<EntityEvent> {

    private final Class<?> entityClass;
    private final EntityOperation entityOperation;
    private final Serializable entityId;

    public EntityEventListener(
            Consumer<EntityEvent> handler,
            Class<?> entityClass,
            EntityOperation entityOperation,
            Serializable entityId
    ) {
        super(EntityEvent.class, (EntityEvent entityEvent) -> {
            if (entityClass != null
                    && !entityClass.isAssignableFrom(entityEvent.getEntityClass())) {
                return;
            }
            if (entityOperation != null
                    && !entityOperation.equals(entityEvent.getEntityOperation())) {
                return;
            }
            if (entityId != null
                    && !entityId.equals(entityEvent.getEntityId())) {
                return;
            }
            handler.accept(entityEvent);
        });
        if (handler == null) {
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
