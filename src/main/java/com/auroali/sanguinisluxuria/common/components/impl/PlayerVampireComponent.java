package com.auroali.sanguinisluxuria.common.components.impl;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.VampireHungerManager;
import com.auroali.sanguinisluxuria.common.abilities.InfectiousAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.events.AllowVampireChangeEvent;
import com.auroali.sanguinisluxuria.common.events.BloodEvents;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.registry.*;
import com.auroali.sanguinisluxuria.config.BLConfig;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.event.GameEvent;

import java.util.UUID;

public class PlayerVampireComponent implements VampireComponent {
    private static final int SYNC_BLOOD_DRAIN = 1;
    private static final int SYNC_SUN_TICKS = 1 << 1;
    private static final int SYNC_ABILITIES = 1 << 2;
    private static final EntityAttributeModifier SPEED_ATTRIBUTE = new EntityAttributeModifier(
      UUID.fromString("a2440a9d-964a-4a84-beac-3c56917cc9fd"),
      "bloodlust.vampire_speed",
      0.02,
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

    public PlayerVampireComponent(PlayerEntity holder) {
        this.holder = holder;
    }

    @Override
    public boolean isVampire() {
        return isVampire;
    }

    @Override
    public void setIsVampire(boolean isVampire) {
        if (!AllowVampireChangeEvent.EVENT.invoker().onChanged(holder, this, isVampire))
            return;

        this.isVampire = isVampire;
        if (!isVampire) {
            removeModifiers();
            timeInSun = 0;
            bloodDrainTimer = 0;
            isDowned = false;
            for (VampireAbility a : abilities) {
                a.onUnVampire(holder, this);
            }
        }
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    @Override
    public void drainBloodFrom(LivingEntity entity) {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
        // if the target doesn't have blood or cannot be drained, we can't fill hunger
        if (!blood.hasBlood() || !BloodEvents.ALLOW_BLOOD_DRAIN.invoker().allowBloodDrain(holder, entity) || !blood.drainBlood(holder))
            return;

        // damage the vampire and cancel filling up hunger if the target has blood protection
        if (entity.hasStatusEffect(BLStatusEffects.BLOOD_PROTECTION)) {
            holder.damage(BLDamageSources.blessedWater(entity), BLConfig.INSTANCE.blessedWaterDamage);
            return;
        }

        // handle differing amounts of blood depending on the good blood tag and unlocked abilities
        int bloodAmount = 1;
        if (!VampireHelper.isVampire(entity) && abilities.hasAbility(BLVampireAbilities.MORE_BLOOD))
            bloodAmount = 2;

        if (!VampireHelper.isVampire(entity) && entity.getType().isIn(BLTags.Entities.GOOD_BLOOD))
            bloodAmount *= 2;

        // try to fill any held blood-storing items first if possible and allowed. if that fails,
        // add to the player's hunger
        if (!VampireHelper.shouldFillHeldItemOnDrain(holder) || !BloodStorageItem.tryAddBloodToItemInHand(holder, bloodAmount)) {
            ((VampireHungerManager) holder.getHungerManager()).sanguinisluxuria$addHunger(bloodAmount, 0.125f);
            BloodEvents.BLOOD_DRAINED.invoker().onBloodDrained(holder, entity, bloodAmount);
        }

        // reset the downed state
        setDowned(false);

        holder.getWorld().emitGameEvent(holder, GameEvent.DRINK, holder.getPos());

        // if the potion transfer ability is unlocked, transfer potion effects to the target
        if (abilities.hasAbility(BLVampireAbilities.TRANSFER_EFFECTS)) {
            BLVampireAbilities.TRANSFER_EFFECTS.sync(entity, InfectiousAbility.InfectiousData.create(entity, holder.getStatusEffects()));
            VampireHelper.transferStatusEffects(holder, target);
        }

        // apply any negative effects for toxic blood
        if (entity.getType().isIn(BLTags.Entities.TOXIC_BLOOD))
            VampireHelper.addToxicBloodEffects(holder);

        // allow conversion of entities with weakness
        if (!VampireHelper.isVampire(entity) && entity.hasStatusEffect(StatusEffects.WEAKNESS)) {
            if (holder instanceof ServerPlayerEntity player)
                BLAdvancementCriterion.INFECT_ENTITY.trigger(player);
            VampireHelper.incrementBloodSickness(entity);
        }

        // villagers have a 50% chance to wake up when having their blood drained
        // it also adds negative reputation to the player
        if (entity.getWorld() instanceof ServerWorld serverWorld && entity instanceof VillagerEntity villager) {
            serverWorld.handleInteraction(EntityInteraction.VILLAGER_HURT, holder, villager);
            if (holder.getRandom().nextDouble() > 0.5f)
                entity.wakeUp();
        }

        if (entity.getType().isIn(BLTags.Entities.TELEPORTS_ON_DRAIN)) {
            VampireHelper.teleportRandomly(holder);
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        isVampire = tag.getBoolean("IsVampire");
        timeInSun = tag.getInt("TimeInSun");
        skillPoints = tag.getInt("SkillPoints");
        level = tag.getInt("Level");
        isDowned = tag.getBoolean("IsDowned");
        abilities.load(tag);
        abilities.setShouldSync(true);
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("IsVampire", isVampire);
        tag.putInt("TimeInSun", timeInSun);
        tag.putInt("SkillPoints", skillPoints);
        tag.putInt("Level", level);
        tag.putBoolean("IsDowned", isDowned);
        abilities.save(tag);
    }

    @Override
    public void serverTick() {
        if (!isVampire)
            return;

        abilities.tick(holder, this);
        if (abilities.needsSync())
            requestSync(SYNC_ABILITIES);

        tickSunEffects();
        tickBloodEffects();

        if (target != null) {
            tickBloodDrain();
        }

        if (needsSync)
            BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    private void removeModifiers() {
        AttributeContainer attributes = holder.getAttributes();
        EntityAttributeInstance speedInstance = attributes.getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedInstance != null && speedInstance.hasModifier(SPEED_ATTRIBUTE))
            speedInstance.removeModifier(SPEED_ATTRIBUTE);
    }

    private void tickBloodEffects() {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(holder);

        if (blood.getBlood() < 6)
            holder.addStatusEffect(new StatusEffectInstance(
              StatusEffects.WEAKNESS,
              4,
              0,
              true,
              true
            ));

        EntityAttributeInstance speedInstance = holder.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedInstance != null && blood.getBlood() > 4 && !speedInstance.hasModifier(SPEED_ATTRIBUTE)) {
            speedInstance.addPersistentModifier(SPEED_ATTRIBUTE);
        } else if (speedInstance != null && blood.getBlood() <= 4 && speedInstance.hasModifier(SPEED_ATTRIBUTE)) {
            speedInstance.removeModifier(SPEED_ATTRIBUTE);
        }
    }

    private void tickSunEffects() {
        if (!isAffectedByDaylight()) {
            if (timeInSun > 0) {
                timeInSun = 0;
                requestSync(SYNC_SUN_TICKS);
            }
            return;
        }

        if (timeInSun >= getMaxTimeInSun() / 2)
            holder.addStatusEffect(new StatusEffectInstance(
              StatusEffects.WEAKNESS,
              4,
              0,
              true,
              true
            ));


        if (timeInSun < getMaxTimeInSun()) {
            timeInSun++;
            requestSync(SYNC_SUN_TICKS);
            return;
        }

        holder.setOnFireFor(6);
    }

    private void tickBloodDrain() {
        updateTarget();
        if (target == null) {
            bloodDrainTimer = 0;
            requestSync(SYNC_BLOOD_DRAIN);
            return;
        }

        targetHasBleeding = target.hasStatusEffect(BLStatusEffects.BLEEDING);
        bloodDrainTimer++;

        target.addStatusEffect(new StatusEffectInstance(
          StatusEffects.SLOWNESS,
          2,
          4,
          true,
          false,
          false
        ));

        if (bloodDrainTimer % 4 == 0)
            holder.getWorld().playSound(
              null,
              holder.getX(),
              holder.getY(),
              holder.getZ(),
              BLSounds.DRAIN_BLOOD,
              SoundCategory.PLAYERS,
              0.5f,
              1.0f
            );

        // need to implement faster draining with bleeding
        int timeToDrain = targetHasBleeding ? BloodConstants.BLOOD_DRAIN_TIME_BLEEDING : BloodConstants.BLOOD_DRAIN_TIME;
        if (bloodDrainTimer >= timeToDrain) {
            drainBloodFrom(target);
            bloodDrainTimer = 0;
        }

        requestSync(SYNC_BLOOD_DRAIN);
    }

    // from MobEntity
    private boolean isAffectedByDaylight() {
        if (holder.getWorld().isDay() && !holder.getWorld().isClient) {
            float f = holder.getBrightnessAtEyes();
            BlockPos blockPos = BlockPos.ofFloored(holder.getX(), holder.getEyeY(), holder.getZ());
            boolean bl = holder.isWet() || holder.inPowderSnow || holder.wasInPowderSnow;
            return f > 0.5F
              && !bl
              && holder.getWorld().isSkyVisible(blockPos);
        }

        return false;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        needsSync = false;
        buf.writeBoolean(isVampire);
        buf.writeBoolean(isDowned);
        // sync blood drain info
        buf.writeBoolean(shouldSync(SYNC_BLOOD_DRAIN));
        if (shouldSync(SYNC_BLOOD_DRAIN)) {
            buf.writeVarInt(bloodDrainTimer);
            buf.writeBoolean(targetHasBleeding);
        }
        // sync time in sun
        buf.writeBoolean(shouldSync(SYNC_SUN_TICKS));
        if (shouldSync(SYNC_SUN_TICKS))
            buf.writeVarInt(timeInSun);
        // sync abilities
        buf.writeBoolean(shouldSync(SYNC_ABILITIES));
        if (shouldSync(SYNC_ABILITIES)) {
            buf.writeInt(level);
            buf.writeInt(skillPoints);
            abilities.writePacket(buf);
            abilities.setShouldSync(false);
        }
        this.syncType = 0;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        isVampire = buf.readBoolean();
        isDowned = buf.readBoolean();

        if (buf.readBoolean()) {
            bloodDrainTimer = buf.readVarInt();
            targetHasBleeding = buf.readBoolean();
        }

        if (buf.readBoolean())
            timeInSun = buf.readVarInt();

        if (buf.readBoolean()) {
            level = buf.readInt();
            skillPoints = buf.readInt();
            abilities.readPacket(buf);
        }
    }

    @Override
    public void tryStartSuckingBlood() {
        if (canDrainBlood() && target == null) {
            updateTarget();
            if (target == null)
                tryToFillStorage();
        }
    }

    private void tryToFillStorage() {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(holder);
        if (blood.getBlood() == 0)
            return;

        if (BloodStorageItem.tryAddBloodToItemInHand(holder, 1))
            blood.drainBlood();
    }

    @Override
    public void stopSuckingBlood() {
        target = null;
        bloodDrainTimer = 0;
        requestSync(SYNC_BLOOD_DRAIN);
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    private boolean canDrainBlood() {
        return !VampireHelper.isMasked(holder);
    }

    @Override
    public int getBloodDrainTimer() {
        return bloodDrainTimer;
    }

    @Override
    public int getMaxTimeInSun() {
        int maxTime = 40;
        ItemStack helmet = holder.getEquippedStack(EquipmentSlot.HEAD);

        if (helmet.isIn(BLTags.Items.SUN_BLOCKING_HELMETS))
            maxTime *= 4;

        if (abilities.hasAbility(BLVampireAbilities.SUN_PROTECTION))
            maxTime += 40;

        int level = EnchantmentHelper.getLevel(BLEnchantments.SUN_PROTECTION, helmet);
        maxTime += level * 20;

        return maxTime;
    }

    @Override
    public int getTimeInSun() {
        return timeInSun;
    }

    @Override
    public VampireAbilityContainer getAbilties() {
        return abilities;
    }

    @Override
    public int getSkillPoints() {
        return skillPoints;
    }

    @Override
    public void unlockAbility(VampireAbility ability) {
        getAbilties().addAbility(ability);
        skillPoints -= ability.getRequiredSkillPoints();
        requestSync(SYNC_ABILITIES);
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
        if (holder instanceof ServerPlayerEntity entity) {
            BLAdvancementCriterion.UNLOCK_ABILITY.trigger(entity, ability);
        }
    }

    @Override
    public boolean isDown() {
        return isDowned;
    }

    @Override
    public void setDowned(boolean down) {
        this.isDowned = down;
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    @Override
    public void setSkillPoints(int i) {
        this.skillPoints = i;
        requestSync(SYNC_BLOOD_DRAIN);
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    @Override
    public void setLevel(int level) {
        this.skillPoints += BLConfig.INSTANCE.skillPointsPerLevel * Math.max(level - this.level, 0);
        this.level = level;
        requestSync(SYNC_BLOOD_DRAIN);
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    @Override
    public int getLevel() {
        return level;
    }

    private void updateTarget() {
        HitResult result = getTarget();
        if (!canDrainBlood() || result.getType() != HitResult.Type.ENTITY) {
            target = null;
            bloodDrainTimer = 0;
            return;
        }

        LivingEntity entity = ((EntityHitResult) result).getEntity() instanceof LivingEntity living ? living : null;

        if (entity == null
          || !entity.getType().isIn(BLTags.Entities.HAS_BLOOD)
          || !BLEntityComponents.BLOOD_COMPONENT.get(entity).hasBlood()
          || BLEntityComponents.BLOOD_COMPONENT.get(entity).getBlood() == 0
        ) {
            target = null;
            bloodDrainTimer = 0;
            return;
        }

        target = entity;
    }

    private HitResult getTarget() {
        double reachDistance = ReachEntityAttributes.getAttackRange(holder, 3.0);
        Vec3d start = holder.getEyePos();
        Vec3d end = start.add(holder.getRotationVector().multiply(reachDistance));

        HitResult result = holder.getWorld().raycast(new RaycastContext(
          start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, holder
        ));

        Vec3d vec3d2 = holder.getRotationVec(1.0F);
        Vec3d vec3d3 = start.add(vec3d2.x * reachDistance, vec3d2.y * reachDistance, vec3d2.z * reachDistance);

        Box box = holder.getBoundingBox().stretch(vec3d2.multiply(reachDistance)).expand(1.0, 1.0, 1.0);

        double d = reachDistance * reachDistance;
        if (result != null)
            d = result.getPos().squaredDistanceTo(start);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(holder, start, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), d);
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
}
