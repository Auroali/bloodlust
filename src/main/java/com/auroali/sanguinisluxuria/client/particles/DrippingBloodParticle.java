package com.auroali.sanguinisluxuria.client.particles;

import com.auroali.sanguinisluxuria.common.registry.BLFluids;
import com.auroali.sanguinisluxuria.common.registry.BLParticles;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

public class DrippingBloodParticle extends BlockLeakParticle.Dripping {
    public DrippingBloodParticle(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle) {
        super(world, x, y, z, fluid, nextParticle);
    }

    public static SpriteBillboardParticle createDrippingBlood(
      DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ
    ) {
        BlockLeakParticle particle = new BlockLeakParticle.Dripping(world, x, y, z, BLFluids.BLOOD, BLParticles.FALLING_BLOOD);
        particle.setColor(1.0f, 1.0f, 1.0f);
        return particle;
    }

    public static SpriteBillboardParticle createFallingBlood(
      DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ
    ) {
        BlockLeakParticle particle = new BlockLeakParticle.ContinuousFalling(world, x, y, z, BLFluids.BLOOD, BLParticles.LANDING_BLOOD);
        particle.setColor(1.0f, 1.0f, 1.0f);
        return particle;
    }

    public static SpriteBillboardParticle createLandingBlood(
      DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ
    ) {
        BlockLeakParticle particle = new BlockLeakParticle.Landing(world, x, y, z, BLFluids.BLOOD);
        particle.setColor(1.0f, 1.0f, 1.0f);
        return particle;
    }
}
