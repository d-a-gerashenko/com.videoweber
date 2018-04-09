package com.videoweber.lib.sampler;

import java.util.ArrayList;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public abstract class EffectProcessor implements SampleHandler {

    private final ArrayList<Effect> effects = new ArrayList<>();
    private SampleHandler sampleHandler = null;

    public final SampleHandler getSampleHandler() {
        return sampleHandler;
    }

    public final void setSampleHandler(SampleHandler sampleHandler) {
        this.sampleHandler = sampleHandler;
    }

    public final ArrayList<Effect> getEffects() {
        return effects;
    }

    @Override
    public void onSample(Sample sample) {
        Sample sampleWithEffects = applyEffects(sample);
        if (sampleHandler != null) {
            sampleHandler.onSample(sampleWithEffects);
        }
    }

    private Sample applyEffects(Sample sample) {
        Sample sampleWithEffect = sample;
        for (Effect effect : getEffects()) {
            if (isSupportedEffect(effect)) {
                sampleWithEffect = applyEffect(sampleWithEffect, effect);
            } else {
                throw new RuntimeException("Unsupported effect: " + effect.getClass().getName());
            }
        }
        return sampleWithEffect;
    }

    public abstract Sample applyEffect(Sample sample, Effect effect);

    public abstract boolean isSupportedEffect(Effect effect);
}
