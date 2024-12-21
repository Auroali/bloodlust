package com.auroali.sanguinisluxuria.common.entities.goals;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class FleeWhenDownedGoal extends Goal {
    public static final float MAX_DISTANCE = 225.f;
    final PathAwareEntity mob;
    final double speed;
    double targetX;
    double targetY;
    double targetZ;

    /**
     * A goal that causes a vampire mob to flee from its attacker when downed
     * <br> The ability must be present in the entity's VampireAbilityContainer
     *
     * @param mob   the vampire entity
     * @param speed the speed at which to move
     */
    public FleeWhenDownedGoal(PathAwareEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!VampireHelper.isVampire(this.mob))
            return false;

        VampireComponent component = BLEntityComponents.VAMPIRE_COMPONENT.get(this.mob);
        if (component.isDown() && this.mob.getAttacker() != null && this.mob.getAttacker().squaredDistanceTo(this.mob) < MAX_DISTANCE) {
            Vec3d target = NoPenaltyTargeting.find(this.mob, 16, 6);
            if (target == null)
                return false;
            this.targetX = target.getX();
            this.targetY = target.getY();
            this.targetZ = target.getZ();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }
}
