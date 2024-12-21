package com.auroali.sanguinisluxuria.common.entities.goals;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class AcivateAbilityWhenDownedGoal extends Goal {
    final MobEntity entity;
    final VampireAbility ability;
    final boolean shouldActivateWhenRecovered;
    boolean isDowned;

    /**
     * A goal that triggers a vampire ability when a vampire entity is downed
     * <br> The ability must be present in the entity's VampireAbilityContainer
     *
     * @param entity                the vampire entity
     * @param ability               the ability to activate
     * @param activateWhenRecovered whether the ability should also activate when the entity recovers
     */
    public AcivateAbilityWhenDownedGoal(MobEntity entity, VampireAbility ability, boolean activateWhenRecovered) {
        this.entity = entity;
        this.ability = ability;
        this.shouldActivateWhenRecovered = activateWhenRecovered;
    }

    @Override
    public boolean canStart() {
        if (!VampireHelper.isVampire(this.entity))
            return false;

        VampireComponent component = BLEntityComponents.VAMPIRE_COMPONENT.get(this.entity);
        if (this.isDowned == component.isDown())
            return false;

        this.isDowned = component.isDown();
        return (this.shouldActivateWhenRecovered || component.isDown()) && component.getAbilties().hasAbility(this.ability);
    }

    @Override
    public boolean canStop() {
        return super.canStop();
    }

    @Override
    public void start() {
        this.ability.activate(this.entity, BLEntityComponents.VAMPIRE_COMPONENT.get(this.entity));
    }
}
