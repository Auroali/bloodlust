package com.auroali.sanguinisluxuria.common.particles;

import com.mojang.serialization.Codec;
import net.minecraft.particle.ParticleType;

public class DelayedParticleType extends ParticleType<DelayedParticleEffect> {
    public DelayedParticleType(boolean alwaysShow) {
        super(alwaysShow, DelayedParticleEffect.FACTORY);
    }

    @Override
    public Codec<DelayedParticleEffect> getCodec() {
        return DelayedParticleEffect.CODEC;
    }
}
