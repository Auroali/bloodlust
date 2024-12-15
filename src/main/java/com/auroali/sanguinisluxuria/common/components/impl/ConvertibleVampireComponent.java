package com.auroali.sanguinisluxuria.common.components.impl;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.InitializableBloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.events.AllowVampireChangeEvent;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

import java.util.function.BiConsumer;

/**
 * Component designed to handle conversion between two entities upon turning into a vampire <br>
 * An example of this would be Villagers turning into Vampire Villagers
 */
public class ConvertibleVampireComponent<U extends LivingEntity, T extends LivingEntity> implements VampireComponent {
    final EntityType<T> conversionType;
    final BiConsumer<U, T> conversionHandler;
    final U holder;

    public ConvertibleVampireComponent(U holder, EntityType<T> conversionType, BiConsumer<U, T> conversionHandler) {
        this.holder = holder;
        this.conversionType = conversionType;
        this.conversionHandler = conversionHandler;
    }

    public ConvertibleVampireComponent(U holder, EntityType<T> conversionType) {
        this(holder, conversionType, (f, t) -> {
        });
    }

    /**
     * Creates a factory for a ConvertibleVampireComponent of the specified type
     *
     * @param type the entity type to convert to
     * @param <T>  the entity class
     * @return the factory
     */
    public static <U extends LivingEntity, T extends LivingEntity> ComponentFactory<U, ? extends VampireComponent> create(EntityType<T> type) {
        return e -> new ConvertibleVampireComponent<>(e, type);
    }

    /**
     * Creates a factory for a ConvertibleVampireComponent of the specified type
     *
     * @param type              the entity type to convert to
     * @param conversionHandler a bi consumer that accepts the original entity and the newly created entity upon conversion, right before it spawns in.
     *                          Can be used for handling custom conversion logic, such as copying certain properties over to the new entity
     * @param <T>               the entity class
     * @return the factory
     */
    public static <U extends LivingEntity, T extends LivingEntity> ComponentFactory<U, ? extends VampireComponent> create(EntityType<T> type, BiConsumer<U, T> conversionHandler) {
        return e -> new ConvertibleVampireComponent<>(e, type, conversionHandler);
    }

    @Override
    public boolean isVampire() {
        return false;
    }

    @Override
    public void setIsVampire(boolean isVampire) {
        if (!isVampire || !AllowVampireChangeEvent.EVENT.invoker().onChanged(this.holder, this, true))
            return;
        T entity = this.conversionType.create(this.holder.getWorld());
        if (entity == null) {
            Bloodlust.LOGGER.error("Could not perform conversion for entity {}!", Registries.ENTITY_TYPE.getId(this.holder.getType()));
            return;
        }
        entity.setPosition(this.holder.getX(), this.holder.getY(), this.holder.getZ());
        entity.setYaw(this.holder.getYaw());
        entity.setPitch(this.holder.getPitch());

        if (this.holder.getType().isIn(BLTags.Entities.HAS_BLOOD) && this.conversionType.isIn(BLTags.Entities.HAS_BLOOD)) {
            BloodComponent component = BLEntityComponents.BLOOD_COMPONENT.get(this.holder);
            BloodComponent newBlood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
            // we do this as otherwise the values we set will be overridden
            if (newBlood instanceof InitializableBloodComponent c)
                c.initializeBloodValues();
            newBlood.setBlood(Math.min(newBlood.getMaxBlood(), component.getBlood()));
        }

        entity.setCustomName(this.holder.getCustomName());


        if (entity instanceof TameableEntity tameable && this.holder instanceof TameableEntity holderTameable && holderTameable.isTamed()) {
            tameable.setOwnerUuid(holderTameable.getOwnerUuid());
            tameable.setTamed(true);
        }

        this.conversionHandler.accept(this.holder, entity);

        this.holder.getWorld().spawnEntity(entity);

        this.holder.getWorld().playSound(null, this.holder.getX(), this.holder.getY(), this.holder.getZ(), BLSounds.VAMPIRE_CONVERT, this.holder.getSoundCategory(), 1.0f, 1.0f);
        this.holder.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public void drainBloodFrom(LivingEntity entity) {
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
        return new VampireAbilityContainer();
    }

    @Override
    public void unlockAbility(VampireAbility ability) {
    }

    @Override
    public boolean isDown() {
        return false;
    }

    @Override
    public void setDowned(boolean down) {
    }

    @Override
    public void serverTick() {
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
    }
}
