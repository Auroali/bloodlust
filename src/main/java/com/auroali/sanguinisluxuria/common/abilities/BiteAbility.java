package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLDamageSources;
import com.auroali.sanguinisluxuria.common.registry.BLParticles;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import com.auroali.sanguinisluxuria.common.registry.BLVampireAbilities;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class BiteAbility extends VampireAbility implements SyncableVampireAbility<LivingEntity> {
    public BiteAbility(Supplier<ItemStack> icon, VampireAbility parent) {
        super(icon, parent);
    }

    @Override
    public boolean isKeybindable() {
        return true;
    }

    @Override
    public void activate(LivingEntity entity, VampireComponent component) {
        if (component.getAbilties().isOnCooldown(this) || VampireHelper.isMasked(entity))
            return;

        HitResult result = this.getTarget(entity);
        if (result.getType() != HitResult.Type.ENTITY)
            return;

        LivingEntity target = ((EntityHitResult) result).getEntity() instanceof LivingEntity e ? e : null;
        if (target == null)
            return;

        target.damage(BLDamageSources.bite(entity), 3);
        target.addStatusEffect(new StatusEffectInstance(BLStatusEffects.BLEEDING, 100, 0));
        this.sync(entity, target);
        if (component.getAbilties().hasAbility(BLVampireAbilities.TRANSFER_EFFECTS)) {
            BLVampireAbilities.TRANSFER_EFFECTS.sync(entity, InfectiousAbility.InfectiousData.create(target, entity.getStatusEffects()));
            VampireHelper.transferStatusEffects(entity, target);
        }
        component.getAbilties().setCooldown(this, 220);
    }

    private HitResult getTarget(LivingEntity entity) {
        double reachDistance = ReachEntityAttributes.getAttackRange(entity, 3.0);
        Vec3d start = entity.getEyePos();
        Vec3d end = start.add(entity.getRotationVector().multiply(reachDistance));

        HitResult result = entity.getWorld().raycast(new RaycastContext(
          start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity
        ));

        Vec3d vec3d2 = entity.getRotationVec(1.0F);
        Vec3d vec3d3 = start.add(vec3d2.x * reachDistance, vec3d2.y * reachDistance, vec3d2.z * reachDistance);

        Box box = entity.getBoundingBox().stretch(vec3d2.multiply(reachDistance)).expand(1.0, 1.0, 1.0);

        double d = reachDistance * reachDistance;
        if (result != null)
            d = result.getPos().squaredDistanceTo(start);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, start, vec3d3, box, e -> !e.isSpectator() && e.canHit(), d);
        if (entityHitResult != null) {
            double g = start.squaredDistanceTo(entityHitResult.getPos());
            if (g < d || result == null) {
                return entityHitResult;
            }
        }
        return result;
    }

    @Override
    public void writePacket(PacketByteBuf buf, World world, LivingEntity data) {
        buf.writeVarInt(data.getId());
    }

    @Override
    public LivingEntity readPacket(PacketByteBuf buf, World world) {
        return (LivingEntity) world.getEntityById(buf.readVarInt());
    }

    @Override
    public void handle(LivingEntity entity, LivingEntity data) {
        Box box = data.getBoundingBox();
        Random rand = data.getRandom();
        int max = 15;
        for (int i = 0; i < max; i++) {
            double x = box.minX + rand.nextDouble() * box.getXLength();
            double y = box.minY + rand.nextDouble() * box.getYLength();
            double z = box.minZ + rand.nextDouble() * box.getZLength();
            data.getWorld().addParticle(
              BLParticles.FALLING_BLOOD,
              x,
              y,
              z,
              0,
              0,
              0
            );
        }
    }
}
