package com.auroali.sanguinisluxuria.common.entities;

import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.entities.goals.TeleportWhenOutOfRangeGoal;
import com.auroali.sanguinisluxuria.common.entities.goals.VampireAttackGoal;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VampireVillagerEntity extends HostileEntity {
    int bloodDrainTimer;

    public VampireVillagerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tickMovement() {
        this.updateDespawnCounter();
        if(this.isAffectedByDaylight()) {
            this.setOnFireFor(8);
        }
        super.tickMovement();
    }

    @Override
    public void tick() {
        super.tick();
        if(bloodDrainTimer > 0)
            bloodDrainTimer--;

        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(this);
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(this);
        if(canHealWithBlood()) {
            setHealth(getHealth() + 1);
            bloodDrainTimer = BloodConstants.BLOOD_DRAIN_TIME * 2;
        }

        if(world.isClient && vampire.isDown()) {
            Box box = getBoundingBox();
            int max = 3;
            for (int i = 0; i < max; i++) {
                double x = box.minX + random.nextDouble() * box.getXLength();
                double y = box.minY + random.nextDouble() * box.getYLength();
                double z = box.minZ + random.nextDouble() * box.getZLength();
                world.addParticle(
                        DustParticleEffect.DEFAULT,
                        x,
                        y,
                        z,
                        0,
                        0,
                        0
                );
            }
        }
    }

    protected boolean canHealWithBlood() {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(this);
        // only heal when healing wouldnt completely drain blood, health is less than max health, and blood could be drained
        // also prioritize draining blood when targeting an entity and health is above 50%
        return blood.getBlood() > 1 && getHealth() < getMaxHealth() && (this.getTarget() == null || this.getHealth() / this.getMaxHealth() < 0.5f) && blood.drainBlood();
    }

    @Override
    public float getMovementSpeed() {
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(this);
        return vampire.isDown() ? 0.5f * super.getMovementSpeed() : super.getMovementSpeed();
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(1, new AvoidSunlightGoal(this));
        this.goalSelector.add(4, new SwimGoal(this));
        this.goalSelector.add(2, new EscapeSunlightGoal(this, 1.0));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(3, new VampireAttackGoal(this, 1.0f, false));
        this.goalSelector.add(2, new TeleportWhenOutOfRangeGoal(this));
        this.goalSelector.add(5, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.goalSelector.add(5, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.goalSelector.add(5, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(6, new ActiveTargetGoal<>(this, LivingEntity.class, true, e -> {
            BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(this);
            return e.getType().isIn(BLTags.Entities.HAS_BLOOD) && ((double) blood.getBlood() / blood.getMaxBlood()) < 0.4;
        }));
    }

    @Override
    public boolean tryAttack(Entity target) {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(this);
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(this);
        if(target instanceof LivingEntity entity && target.getType().isIn(BLTags.Entities.HAS_BLOOD) && bloodDrainTimer == 0 && blood.getBlood() < blood.getMaxBlood()) {
            vampire.drainBloodFrom(entity);
            playSound(BLSounds.DRAIN_BLOOD, 1.0f, 1.0f);
            bloodDrainTimer = BloodConstants.BLOOD_DRAIN_TIME * 2;
            this.onAttacking(target);
            return true;
        }
        return super.tryAttack(target);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("BloodDrainTimer", bloodDrainTimer);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        bloodDrainTimer = nbt.getInt("BloodDrainTimer");
    }
}
