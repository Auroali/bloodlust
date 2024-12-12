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
        this.defaultAbilities = Arrays.stream(abilities).toList();
        this.defaultAbilities.forEach(this.abilities::addAbility);
    }

    public EntityVampireComponent(T holder, VampireAbility... abilities) {
        this(holder, Predicates.alwaysTrue(), abilities);
    }

    @Override
    public boolean isVampire() {
        return this.vampirePredicate.test(this.holder);
    }

    @Override
    public void setIsVampire(boolean isVampire) {

    }

    @Override
    public void drainBloodFrom(LivingEntity entity) {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
        BloodComponent holderBlood = BLEntityComponents.BLOOD_COMPONENT.get(this.holder);
        if (!blood.hasBlood() || !BloodEvents.ALLOW_BLOOD_DRAIN.invoker().allowBloodDrain(this.holder, entity) || !blood.drainBlood(this.holder))
            return;

        // damage the vampire and cancel filling up hunger if the target has blood protection
        if (entity.hasStatusEffect(BLStatusEffects.BLOOD_PROTECTION)) {
            this.holder.damage(BLDamageSources.blessedWater(entity), BLConfig.INSTANCE.blessedWaterDamage);
            return;
        }

        // handle differing amounts of blood depending on the good blood tag and unlocked abilities
        int bloodAmount = 1;

        if (!VampireHelper.isVampire(entity) && entity.getType().isIn(BLTags.Entities.GOOD_BLOOD))
            bloodAmount *= 2;

        holderBlood.addBlood(bloodAmount);
        BloodEvents.BLOOD_DRAINED.invoker().onBloodDrained(this.holder, entity, bloodAmount);

        this.setDowned(false);
        this.holder.getWorld().emitGameEvent(this.holder, GameEvent.DRINK, this.holder.getPos());

        // if the potion transfer ability is unlocked, transfer potion effects to the target
        if (this.abilities.hasAbility(BLVampireAbilities.INFECTIOUS)) {
            BLVampireAbilities.INFECTIOUS.sync(entity, InfectiousAbility.InfectiousData.create(entity, this.holder.getStatusEffects()));
            this.transferPotionEffectsTo(entity);
        }

        // apply any negative effects for toxic blood
        if (entity.getType().isIn(BLTags.Entities.TOXIC_BLOOD))
            this.addToxicBloodEffects();

        // allow conversion of entities with weakness
        if (!VampireHelper.isVampire(entity) && entity.hasStatusEffect(StatusEffects.WEAKNESS)) {
            if (this.holder instanceof ServerPlayerEntity player)
                BLAdvancementCriterion.INFECT_ENTITY.trigger(player);
            this.addBloodSickness(entity);
        }

        // villagers have a 50% chance to wake up when having their blood drained
        // it also adds negative reputation to the player
        if (entity.getWorld() instanceof ServerWorld serverWorld && entity instanceof VillagerEntity villager) {
            serverWorld.handleInteraction(EntityInteraction.VILLAGER_HURT, this.holder, villager);
            if (this.holder.getRandom().nextDouble() > 0.5f)
                entity.wakeUp();
        }

        if (entity.getType().isIn(BLTags.Entities.TELEPORTS_ON_DRAIN)) {
            VampireHelper.teleportRandomly(this.holder);
        }
    }

    private void addToxicBloodEffects() {
        this.holder.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 3));
        this.holder.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
        if (this.holder.getRandom().nextDouble() > 0.75)
            this.holder.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));
    }

    private void transferPotionEffectsTo(LivingEntity entity) {
        for (StatusEffectInstance instance : this.holder.getStatusEffects()) {
            entity.addStatusEffect(instance);
        }

        if (this.holder instanceof ServerPlayerEntity player) {
            BLAdvancementCriterion.TRANSFER_EFFECTS.trigger(player, player.getStatusEffects().size());
        }

        this.holder.clearStatusEffects();
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
        return this.abilities;
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
        this.abilities.addAbility(ability);
    }

    @Override
    public boolean isDown() {
        return this.downed;
    }

    @Override
    public void setDowned(boolean down) {
        this.downed = down;
        BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    @Override
    public void serverTick() {
        this.abilities.tick(this.holder, this);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.downed = tag.getBoolean("Downed");
        this.abilities.load(tag);
        // ensure that all default abilities are present
        this.defaultAbilities.forEach(this.abilities::addAbility);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("Downed", this.downed);
        this.abilities.save(tag);
    }
}
