package com.auroali.sanguinisluxuria.common.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.LivingEntity;

public interface BloodComponent extends Component, AutoSyncedComponent {
    /**
     * @return the amount of blood the component holder has
     */
    int getBlood();

    /**
     * @return the maximum amount of blood the component holder can have
     */
    int getMaxBlood();

    /**
     * Adds blood to the current amount
     *
     * @param amount the amount to add
     * @return the amount actually added
     */
    int addBlood(int amount);

    /**
     * Sets the current amount of blood
     *
     * @param amount the new amount of blood
     */
    void setBlood(int amount);

    /**
     * Tries to drain one unit of blood
     *
     * @param drainer the entity draining the component holder's blood
     * @return whether blood was actually drained
     * @see BloodComponent#drainBlood()
     */
    boolean drainBlood(LivingEntity drainer);

    /**
     * Tries to drain one unit of blood
     *
     * @return whether blood was actually drained
     * @see BloodComponent#drainBlood(LivingEntity)
     */
    boolean drainBlood();

    /**
     * @return whether the component holder has blood
     */
    boolean isEmpty();
}
