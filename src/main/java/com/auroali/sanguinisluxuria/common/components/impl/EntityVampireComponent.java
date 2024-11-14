package com.auroali.sanguinisluxuria.common.components.impl;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.InfectiousAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.events.BloodEvents;
import com.auroali.sanguinisluxuria.common.registry.*;
import com.auroali.sanguinisluxuria.config.BLConfig;
import com.google.common.base.Predicates;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.event.GameEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class EntityVampireComponent<T extends LivingEntity> implements VampireComponent {
    final Predicate<T> vampirePredicate;
    final T holder;
    final VampireAbilityContainer abilities = new VampireAbilityContainer();
    final List<VampireAbility> defaultAbilities;
    boolean downed;

    public EntityVampireComponent(T holder, Predicate<T> vampirePredicate, VampireAbility... abilities) {
        this.holder = holder;
        this.vampirePredicate = vampirePredicate;
        defaultAbilities = Arrays.stream(abilities).toList();
        defaultAbilities.forEach(this.abilities::addAbility);
    }

    public EntityVampireComponent(T holder, VampireAbility... abilities) {
        this(holder, Predicates.alwaysTrue(), abilities);
    }

    @Override
    public boolean isVampire() {
        return vampirePredicate.test(holder);
    }

    @Override
    public void setIsVampire(boolean isVampire) {

    }

    @Override
    public void drainBloodFrom(LivingEntity entity) {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
        BloodComponent holderBlood = BLEntityComponents.BLOOD_COMPONENT.get(holder);
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

        holderBlood.addBlood(bloodAmount);
        BloodEvents.BLOOD_DRAINED.invoker().onBloodDrained(holder, entity, bloodAmount);

        setDowned(false);
        holder.getWorld().emitGameEvent(holder, GameEvent.DRINK, holder.getPos());

        // if the potion transfer ability is unlocked, transfer potion effects to the target
        if (abilities.hasAbility(BLVampireAbilities.TRANSFER_EFFECTS)) {
            BLVampireAbilities.TRANSFER_EFFECTS.sync(entity, InfectiousAbility.InfectiousData.create(entity, holder.getStatusEffects()));
            transferPotionEffectsTo(entity);
        }

        // apply any negative effects for toxic blood
        if (entity.getType().isIn(BLTags.Entities.TOXIC_BLOOD))
            addToxicBloodEffects();

        // allow conversion of entities with weakness
        if (!VampireHelper.isVampire(entity) && entity.hasStatusEffect(StatusEffects.WEAKNESS)) {
            if (holder instanceof ServerPlayerEntity player)
                BLAdvancementCriterion.INFECT_ENTITY.trigger(player);
            addBloodSickness(entity);
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

    private void addToxicBloodEffects() {
        holder.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 3));
        holder.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
        if (holder.getRandom().nextDouble() > 0.75)
            holder.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));
    }

    private void transferPotionEffectsTo(LivingEntity entity) {
        for (StatusEffectInstance instance : holder.getStatusEffects()) {
            entity.addStatusEffect(instance);
        }

        if (holder instanceof ServerPlayerEntity player) {
            BLAdvancementCriterion.TRANSFER_EFFECTS.trigger(player, player.getStatusEffects().size());
        }

        holder.clearStatusEffects();
    }

    private void addBloodSickness(LivingEntity target) {
        int level = target.hasStatusEffect(BLStatusEffects.BLOOD_SICKNESS)
          ? target.getStatusEffect(BLStatusEffects.BLOOD_SICKNESS).getAmplifier() + 1
          : 0;

        target.addStatusEffect(new StatusEffectInstance(BLStatusEffects.BLOOD_SICKNESS, 3600, level));
    }

    @Override
    public void tryStartSuckingBlood() {

    }

    @Override
    public void stopSuckingBlood() {

    }

    @Override
    public int getBloodDrainTimer() {
        return 0;
    }

    @Override
    public int getMaxTimeInSun() {
        return 0;
    }

    @Override
    public int getTimeInSun() {
        return 0;
    }

    @Override
    public VampireAbilityContainer getAbilties() {
        return abilities;
    }

    @Override
    public int getSkillPoints() {
        return 0;
    }

    @Override
    public void setSkillPoints(int points) {

    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void unlockAbility(VampireAbility ability) {
        abilities.addAbility(ability);
    }

    @Override
    public boolean isDown() {
        return downed;
    }

    @Override
    public void setDowned(boolean down) {
        this.downed = down;
        BLEntityComponents.VAMPIRE_COMPONENT.sync(holder);
    }

    @Override
    public void serverTick() {
        abilities.tick(holder, this);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        downed = tag.getBoolean("Downed");
        abilities.load(tag);
        // ensure that all default abilities are present
        defaultAbilities.forEach(abilities::addAbility);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("Downed", downed);
        abilities.save(tag);
    }
}
