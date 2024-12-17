package com.auroali.sanguinisluxuria.common.entities.goals;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.registry.BLVampireAbilities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

import java.util.EnumSet;

public class TeleportWhenOutOfRangeGoal extends Goal {
    private final MobEntity mob;
    private LivingEntity target;

    public TeleportWhenOutOfRangeGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.mob.getTarget();
        VampireAbilityContainer container = BLEntityComponents.VAMPIRE_COMPONENT.get(this.mob).getAbilties();
        if (!container.hasAbility(BLVampireAbilities.TELEPORT))
            return false;

        if (livingEntity == null || !this.mob.getVisibilityCache().canSee(livingEntity) || container.isOnCooldown(BLVampireAbilities.TELEPORT) || livingEntity.getPos().distanceTo(this.mob.getPos()) < BLVampireAbilities.TELEPORT.getRange(this.mob)) {
            return false;
        } else {
            this.target = livingEntity;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        VampireAbilityContainer container = BLEntityComponents.VAMPIRE_COMPONENT.get(this.mob).getAbilties();
        double teleportRange = Math.pow(BLVampireAbilities.TELEPORT.getRange(this.mob), 2);

        if (!this.target.isAlive()) {
            return false;
        } else if (!this.mob.getVisibilityCache().canSee(this.target) || container.isOnCooldown(BLVampireAbilities.TELEPORT) || this.mob.squaredDistanceTo(this.target) < teleportRange) {
            return false;
        } else {
            return !this.mob.getNavigation().isIdle() || this.canStart();
        }
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().lookAt(this.target, 360F, 360F);
        BLVampireAbilities.TELEPORT.activate(this.mob, BLEntityComponents.VAMPIRE_COMPONENT.get(this.mob));
    }
}