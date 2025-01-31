package com.auroali.sanguinisluxuria.common.components.impl;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.EntityTrackingDrainer;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.events.VampireSunEvents;
import com.auroali.sanguinisluxuria.common.items.EntityTrackingItem;
import com.auroali.sanguinisluxuria.common.registry.*;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.UUID;

public class PlayerVampireComponent implements VampireComponent, EntityTrackingDrainer {
    private static final int SYNC_BLOOD_DRAIN = 1;
    private static final int SYNC_SUN_TICKS = 1 << 1;
    private static final int SYNC_ABILITIES = 1 << 2;
    private static final int ENTITY_TRACKING_TICKS = 3600;
    private static final EntityAttributeModifier SPEED_ATTRIBUTE = new EntityAttributeModifier(
      UUID.fromString("a2440a9d-964a-4a84-beac-3c56917cc9fd"),
      "bloodlust.vampire_speed",
      0.02,
      EntityAttributeModifier.Operation.ADDITION
    );
    private static final EntityAttributeModifier HEALTH_ATTRIBUTE = new EntityAttributeModifier(
      UUID.fromString("92d94e2c-1582-44f4-8563-c8341af52efb"),
      "bloodlust.vampire_health",
      4,
      EntityAttributeModifier.Operation.ADDITION
    );

    private boolean needsSync;
    private int syncType;
    public boolean targetHasBleeding;
    private final VampireAbilityContainer abilities = new VampireAbilityContainer();
    private final PlayerEntity holder;
    private boolean isVampire;
    private LivingEntity target;
    private int bloodDrainTimer;
    private int timeInSun;
    private int skillPoints;
    private int level;
    private boolean isDowned;
    private boolean isMist;
    private Entity lastDrainedEntity;
    private int ticksEntityTracked;

    public PlayerVampireComponent(PlayerEntity holder) {
        this.holder = holder;
    }

    @Override
    public boolean isVampire() {
        return this.isVampire;
    }

    @Override
    public void setIsVampire(boolean isVampire) {
        this.isVampire = isVampire;
        if (!isVampire) {
            this.removeModifiers();
            this.timeInSun = 0;
            this.bloodDrainTimer = 0;
            this.isDowned = false;
            for (VampireAbility a : this.abilities) {
                a.onUnVampire(this.holder, this);
            }
        }
        BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    @Override
    public void drainBloodFrom(LivingEntity entity) {
        VampireComponent.handleBloodDrain(this, entity, this.holder);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.isVampire = tag.getBoolean("IsVampire");
        this.timeInSun = tag.getInt("TimeInSun");
        this.skillPoints = tag.getInt("SkillPoints");
        this.level = tag.getInt("Level");
        this.isDowned = tag.getBoolean("IsDowned");
        this.abilities.load(tag);
        this.abilities.setShouldSync(true);
        BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("IsVampire", this.isVampire);
        tag.putInt("TimeInSun", this.timeInSun);
        tag.putInt("SkillPoints", this.skillPoints);
        tag.putInt("Level", this.level);
        tag.putBoolean("IsDowned", this.isDowned);
        this.abilities.save(tag);
    }

    @Override
    public void serverTick() {
        if (!this.isVampire)
            return;

        this.abilities.tick(this.holder, this);
        if (this.abilities.needsSync())
            this.requestSync(SYNC_ABILITIES);

        this.tickSunEffects();
        this.tickBloodEffects();

        if (this.isDowned) {
            this.holder.addStatusEffect(new StatusEffectInstance(
              StatusEffects.WEAKNESS,
              4,
              3,
              true,
              false,
              false
            ));
        }

        if (this.target != null) {
            this.tickBloodDrain();
        }

        // handle entity tracking
        if (this.lastDrainedEntity != null && !this.lastDrainedEntity.isAlive())
            this.lastDrainedEntity = null;

        if (this.lastDrainedEntity != null) {
            this.ticksEntityTracked++;
            if (this.ticksEntityTracked > ENTITY_TRACKING_TICKS)
                this.lastDrainedEntity = null;
        }

        if (this.needsSync)
            BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    private void removeModifiers() {
        AttributeContainer attributes = this.holder.getAttributes();
        EntityAttributeInstance speedInstance = attributes.getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedInstance != null && speedInstance.hasModifier(SPEED_ATTRIBUTE))
            speedInstance.removeModifier(SPEED_ATTRIBUTE);
    }

    private void tickBloodEffects() {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(this.holder);

        if (blood.getBlood() < 6)
            this.holder.addStatusEffect(new StatusEffectInstance(
              StatusEffects.WEAKNESS,
              4,
              0,
              true,
              true
            ));

        VampireHelper.applyModifierFromBlood(this.holder, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_ATTRIBUTE, blood, b -> b.getBlood() >= 4);
        VampireHelper.applyModifierFromBlood(this.holder, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_ATTRIBUTE, blood, b -> b.getBlood() >= 2);
    }

    private void tickSunEffects() {
        if (!this.isAffectedByDaylight()) {
            if (this.timeInSun > 0) {
                this.timeInSun = 0;
                this.requestSync(SYNC_SUN_TICKS);
            }
            return;
        }

        if (this.timeInSun >= 1)
            this.holder.addStatusEffect(new StatusEffectInstance(
              StatusEffects.WEAKNESS,
              4,
              0,
              true,
              true
            ));


        if (this.timeInSun < this.getMaxTimeInSun()) {
            this.timeInSun++;
            this.requestSync(SYNC_SUN_TICKS);
            return;
        }

        this.holder.setOnFireFor(6);
    }

    private void tickBloodDrain() {
        this.updateTarget();
        if (this.target == null) {
            this.bloodDrainTimer = 0;
            this.requestSync(SYNC_BLOOD_DRAIN);
            return;
        }

        this.targetHasBleeding = this.target.hasStatusEffect(BLStatusEffects.BLEEDING);
        this.bloodDrainTimer++;

        this.target.addStatusEffect(new StatusEffectInstance(
          StatusEffects.SLOWNESS,
          2,
          4,
          true,
          false,
          false
        ));

        if (this.bloodDrainTimer % 4 == 0)
            this.holder.getWorld().playSound(
              null,
              this.holder.getX(),
              this.holder.getY(),
              this.holder.getZ(),
              BLSounds.DRAIN_BLOOD,
              SoundCategory.PLAYERS,
              0.5f,
              1.0f
            );

        // need to implement faster draining with bleeding
        int timeToDrain = this.targetHasBleeding ? BloodConstants.BLOOD_DRAIN_TIME_BLEEDING : BloodConstants.BLOOD_DRAIN_TIME;
        if (this.bloodDrainTimer >= timeToDrain) {
            this.drainBloodFrom(this.target);
            this.bloodDrainTimer = 0;
        }

        this.requestSync(SYNC_BLOOD_DRAIN);
    }

    // from MobEntity
    private boolean isAffectedByDaylight() {
        if (this.holder.getWorld().isDay() && !this.holder.getWorld().isClient) {
            float f = this.holder.getBrightnessAtEyes();
            BlockPos blockPos = BlockPos.ofFloored(this.holder.getX(), this.holder.getEyeY(), this.holder.getZ());
            boolean bl = this.holder.isWet() || this.holder.inPowderSnow || this.holder.wasInPowderSnow;
            return f > 0.5F
              && !bl
              && this.holder.getWorld().isSkyVisible(blockPos)
              && VampireSunEvents.CAN_BURN.invoker().canBurn(this.holder.getWorld(), this.holder, this);
        }
        return false;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        this.needsSync = false;
        buf.writeBoolean(this.isVampire);
        buf.writeBoolean(this.isDowned);
        buf.writeBoolean(this.isMist);
        // sync blood drain info
        buf.writeBoolean(this.shouldSync(SYNC_BLOOD_DRAIN));
        if (this.shouldSync(SYNC_BLOOD_DRAIN)) {
            buf.writeVarInt(this.bloodDrainTimer);
            buf.writeBoolean(this.targetHasBleeding);
        }
        // sync time in sun
        buf.writeBoolean(this.shouldSync(SYNC_SUN_TICKS));
        if (this.shouldSync(SYNC_SUN_TICKS))
            buf.writeVarInt(this.timeInSun);
        // sync abilities
        buf.writeBoolean(this.shouldSync(SYNC_ABILITIES));
        if (this.shouldSync(SYNC_ABILITIES)) {
            buf.writeInt(this.level);
            buf.writeInt(this.skillPoints);
            this.abilities.writePacket(buf);
            this.abilities.setShouldSync(false);
        }
        this.syncType = 0;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.isVampire = buf.readBoolean();
        this.isDowned = buf.readBoolean();
        this.isMist = buf.readBoolean();

        if (buf.readBoolean()) {
            this.bloodDrainTimer = buf.readVarInt();
            this.targetHasBleeding = buf.readBoolean();
        }

        if (buf.readBoolean())
            this.timeInSun = buf.readVarInt();

        if (buf.readBoolean()) {
            this.level = buf.readInt();
            this.skillPoints = buf.readInt();
            this.abilities.readPacket(buf);
        }
    }

    @Override
    public void tryStartSuckingBlood() {
        if (this.canDrainBlood() && this.target == null) {
            this.updateTarget();
            if (this.target == null)
                this.tryToFillStorage();
        }
    }

    private void tryToFillStorage() {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(this.holder);
        if (blood.getBlood() == 0)
            return;

        if (VampireHelper.fillHeldBloodStorage(this.holder, 1, stack -> {
            if (EntityTrackingItem.canTrackEntity(stack) && EntityTrackingItem.getEntity(stack) == null && this.getLastDrained() != null) {
                EntityTrackingItem.setEntity(stack, this.getLastDrained());
                this.setLastDrained(null);
            }
        })) {
            blood.drainBlood();
        }
    }

    @Override
    public void stopSuckingBlood() {
        this.target = null;
        this.bloodDrainTimer = 0;
        this.requestSync(SYNC_BLOOD_DRAIN);
        BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    private boolean canDrainBlood() {
        return !VampireHelper.isMasked(this.holder) && !this.isMist();
    }

    @Override
    public int getBloodDrainTimer() {
        return this.bloodDrainTimer;
    }

    @Override
    public int getMaxTimeInSun() {
        int maxTime = 40;
        ItemStack helmet = this.holder.getEquippedStack(EquipmentSlot.HEAD);

        if (helmet.isIn(BLTags.Items.SUN_BLOCKING_HELMETS))
            maxTime += 80;

        int level = EnchantmentHelper.getLevel(BLEnchantments.SUN_PROTECTION, helmet);
        maxTime += level * 20;

        double sunResistance = this.holder.getAttributeValue(BLEntityAttributes.SUN_RESISTANCE);
        return (int) (sunResistance * VampireSunEvents.MODIFY_SUN_TIME.invoker().getMaxTimeInSun(this.holder, this, maxTime));
    }

    @Override
    public int getTimeInSun() {
        return this.timeInSun;
    }

    @Override
    public VampireAbilityContainer getAbilties() {
        return this.abilities;
    }

    @Override
    public void unlockAbility(VampireAbility ability) {
        this.getAbilties().addAbility(ability);
        this.requestSync(SYNC_ABILITIES);
        if (this.holder instanceof ServerPlayerEntity entity) {
            BLAdvancementCriterion.UNLOCK_ABILITY.trigger(entity, ability);
        }
    }

    @Override
    public boolean isDown() {
        return this.isDowned;
    }

    @Override
    public void setDowned(boolean down) {
        this.isDowned = down;
        this.isMist = false;
        BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    @Override
    public boolean isMist() {
        return this.isMist;
    }

    @Override
    public void setMist(boolean isMist) {
        this.isMist = isMist;
        BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    private void updateTarget() {
        HitResult result = this.getTarget();
        if (!this.canDrainBlood() || result.getType() != HitResult.Type.ENTITY) {
            this.target = null;
            this.bloodDrainTimer = 0;
            return;
        }

        LivingEntity entity = ((EntityHitResult) result).getEntity() instanceof LivingEntity living ? living : null;

        if (!VampireHelper.hasBlood(entity)
          || !BLEntityComponents.BLOOD_COMPONENT.get(entity).isEmpty()
          || BLEntityComponents.BLOOD_COMPONENT.get(entity).getBlood() == 0
        ) {
            this.target = null;
            this.bloodDrainTimer = 0;
            return;
        }

        this.target = entity;
    }

    private HitResult getTarget() {
        double reachDistance = ReachEntityAttributes.getAttackRange(this.holder, 3.0);
        Vec3d start = this.holder.getEyePos();
        Vec3d end = start.add(this.holder.getRotationVector().multiply(reachDistance));

        HitResult result = this.holder.getWorld().raycast(new RaycastContext(
          start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this.holder
        ));

        Vec3d vec3d2 = this.holder.getRotationVec(1.0F);
        Vec3d vec3d3 = start.add(vec3d2.x * reachDistance, vec3d2.y * reachDistance, vec3d2.z * reachDistance);

        Box box = this.holder.getBoundingBox().stretch(vec3d2.multiply(reachDistance)).expand(1.0, 1.0, 1.0);

        double d = reachDistance * reachDistance;
        if (result != null)
            d = result.getPos().squaredDistanceTo(start);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(this.holder, start, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), d);
        if (entityHitResult != null) {
            double g = start.squaredDistanceTo(entityHitResult.getPos());
            if (g < d || result == null) {
                return entityHitResult;
            }
        }
        return result;
    }

    private void requestSync(int flags) {
        this.syncType |= flags;
        this.needsSync = true;
    }

    private boolean shouldSync(int flag) {
        return this.syncType == 0 || (this.syncType & flag) != 0;
    }

    @Override
    public void setLastDrained(Entity entity) {
        this.lastDrainedEntity = entity;
    }

    @Override
    public Entity getLastDrained() {
        return this.lastDrainedEntity;
    }
}
