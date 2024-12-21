package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class MistAbility extends VampireAbility implements EntitySyncableVampireAbility<LivingEntity> {
    @Override
    public void activate(LivingEntity entity, VampireComponent component) {
        if (!component.isMist() && component.getAbilties().isOnCooldown(this))
            return;

        boolean isMist = !component.isMist();
        // set the cooldown first to avoid a double sync
        this.setCooldown(component.getAbilties(), isMist);
        component.setMist(isMist);
        if (isMist)
            this.sync(entity, entity);
    }

    void setCooldown(VampireAbilityContainer container, boolean isMist) {
        if (isMist)
            container.setCooldown(this, 200);
            // acts as a timer for the ability
        else container.setCooldown(this, 3600);
    }

    @Override
    public void onCooldownEnd(LivingEntity entity, VampireComponent component, VampireAbilityContainer container) {
        if (component.isMist()) {
            this.setCooldown(container, false);
            component.setMist(false);
        }
    }

    @Override
    public void handle(LivingEntity entity, LivingEntity data) {
        World world = entity.getWorld();
        Random random = entity.getRandom();
        Box boundingBox = entity.getBoundingBox();
        int numParticles = (int) (20 * entity.getBoundingBox().getAverageSideLength());
        for (int i = 0; i < numParticles; i++) {
            double x = boundingBox.minX + (random.nextFloat() * boundingBox.getXLength());
            double y = boundingBox.minY + (random.nextFloat() * boundingBox.getYLength());
            double z = boundingBox.minZ + (random.nextFloat() * boundingBox.getZLength());
            double velocityX = random.nextGaussian() * 0.05d;
            double velocityY = random.nextGaussian() * 0.05d;
            double velocityZ = random.nextGaussian() * 0.05d;
            world.addParticle(ParticleTypes.SMOKE, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}
