package com.auroali.sanguinisluxuria.common.components.impl;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.google.common.base.Predicates;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class EntityVampireComponent<T extends LivingEntity> implements VampireComponent {
    final Predicate<T> vampirePredicate;
    final T holder;
    final VampireAbilityContainer abilities = new VampireAbilityContainer();
    final List<VampireAbility> defaultAbilities;
    boolean downed;
    boolean isMist;

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
        VampireComponent.handleBloodDrain(this, entity, this.holder);
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
    public boolean isMist() {
        return this.isMist;
    }

    @Override
    public void setMist(boolean isMist) {
        this.isMist = isMist;
        BLEntityComponents.VAMPIRE_COMPONENT.sync(this.holder);
    }

    @Override
    public void serverTick() {
        this.abilities.tick(this.holder, this);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.downed = tag.getBoolean("Downed");
        this.isMist = tag.getBoolean("IsMist");
        this.abilities.load(tag);
        // ensure that all default abilities are present
        this.defaultAbilities.forEach(this.abilities::addAbility);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("Downed", this.downed);
        tag.putBoolean("IsMist", this.isMist);
        this.abilities.save(tag);
    }
}
