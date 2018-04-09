package com.videoweber.server.service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.sampler.Effect;
import com.videoweber.lib.sampler.effects.Rotate;
import com.videoweber.server.entity.EffectEntity;
import com.videoweber.server.entity.EffectRotateEntity;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class EffectManagerService extends Service {

    public EffectManagerService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public Effect createEffect(EffectEntity effectEntity) {
        if (effectEntity == null) {
            throw new NullPointerException();
        }
        if (effectEntity instanceof EffectRotateEntity) {
            Rotate effect = new Rotate(((EffectRotateEntity) effectEntity).getAngel());
            return effect;
        }
        throw new RuntimeException("Unsupported effect: " + effectEntity.getClass().getName());
    }

}
