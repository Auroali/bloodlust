package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.BloodTransferComponent;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileEntity {
    @Shadow
    protected abstract boolean isOwnerAlive();

    @Unique
    private int sanguinisluxuria$latchedTicks = 0;

    protected TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/TridentEntity;getOwner()Lnet/minecraft/entity/Entity;", shift = At.Shift.BY, by = 2), cancellable = true)
    public void sanguinisluxuria$handleBloodDrainLogic(CallbackInfo ci, @Local(ordinal = 0) Entity owner) {
        BloodTransferComponent bloodTransfer = BLEntityComponents.BLOOD_TRANSFER_COMPONENT.get(this);
        int bloodDrainLevel = bloodTransfer.getBloodTransferLevel();
        Entity latched = bloodTransfer.getLatchedEntity();
        if (bloodDrainLevel != 0 && latched != null && latched.isAlive() && !latched.isRemoved()) {
            if (!this.isOwnerAlive()) {
                if (!this.getWorld().isClient && this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1F);
                }
                this.discard();
                ci.cancel();
                return;
            }
            if (this.sanguinisluxuria$latchedTicks > 300) {
                bloodTransfer.setLatchedEntity(null);
                this.sanguinisluxuria$latchedTicks = 0;
                return;
            }

            BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(latched);
            BloodComponent ownerBlood = BLEntityComponents.BLOOD_COMPONENT.get(owner);

            if ((latched instanceof LivingEntity livingTarget && livingTarget.hasStatusEffect(BLStatusEffects.BLOOD_PROTECTION)) || blood.getBlood() <= Math.max(1, blood.getMaxBlood() / (1 + bloodDrainLevel))) {
                bloodTransfer.setLatchedEntity(null);
                this.sanguinisluxuria$latchedTicks = 0;
                return;
            }

            this.setPosition(latched.getPos().add(0, latched.getEyeHeight(latched.getPose()) * 0.75, 0));
            this.setVelocity(Vec3d.ZERO);

            int timeToDrain = latched instanceof LivingEntity e && e.hasStatusEffect(BLStatusEffects.BLEEDING) ? 20 : 40;
            if (this.sanguinisluxuria$latchedTicks % timeToDrain == 0 && !this.getWorld().isClient && blood.drainBlood()) {
                if (!(owner instanceof LivingEntity entity && VampireHelper.fillHeldBloodStorage(entity, 1)) && VampireHelper.isVampire(owner)) {
                    ownerBlood.addBlood(1);
                }
                this.playSound(BLSounds.DRAIN_BLOOD, 1.0f, 1.0f);
            }

            this.sanguinisluxuria$latchedTicks++;
            ci.cancel();
        }
    }

    @Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/TridentEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    public void sanguinisluxuria$latchOnEntity(EntityHitResult entityHitResult, CallbackInfo ci, @Local(ordinal = 0) Entity target, @Local(ordinal = 1) Entity owner) {
        BloodTransferComponent bloodTransfer = BLEntityComponents.BLOOD_TRANSFER_COMPONENT.get(this);
        if (bloodTransfer.getBloodTransferLevel() != 0 && target.getType().isIn(BLTags.Entities.HAS_BLOOD)) {
            if (target instanceof LivingEntity livingTarget && livingTarget.hasStatusEffect(BLStatusEffects.BLOOD_PROTECTION))
                return;
            bloodTransfer.setLatchedEntity(target);
            this.sanguinisluxuria$latchedTicks = 0;
        }
    }
}
