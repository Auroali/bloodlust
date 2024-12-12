package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.particles.DelayedParticleType;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BLParticles {
    public static final DefaultParticleType DRIPPING_BLOOD = FabricParticleTypes.simple();
    public static final DefaultParticleType FALLING_BLOOD = FabricParticleTypes.simple();
    public static final DefaultParticleType LANDING_BLOOD = FabricParticleTypes.simple();
    public static final DelayedParticleType ALTAR_BEAT = new DelayedParticleType(false);

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, BLResources.DRIPPING_BLOOD, DRIPPING_BLOOD);
        Registry.register(Registries.PARTICLE_TYPE, BLResources.FALLING_BLOOD, FALLING_BLOOD);
        Registry.register(Registries.PARTICLE_TYPE, BLResources.LANDING_BLOOD, LANDING_BLOOD);
        Registry.register(Registries.PARTICLE_TYPE, BLResources.ALTAR_BEAT_PARTICLE, ALTAR_BEAT);
    }
}
