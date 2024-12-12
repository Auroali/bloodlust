package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLEntityAttributes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected abstract boolean tryUseTotem(DamageSource source);

    @Shadow
    public abstract boolean isUndead();

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At(value = "HEAD"))
    public void sanguinisluxuria$increaseDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("source") LocalRef<DamageSource> dmgSource) {
        dmgSource.set(source);
    }

    @ModifyVariable(method = "damage", at = @At(
      value = "HEAD"
    ), argsOnly = true)
    public float sanguinisluxuria$actuallyIncreaseDamage(float amount, @Share("source") LocalRef<DamageSource> source) {
        float blessedDamageMod = 0.0f;
        if (this.isUndead() && source.get().getAttacker() instanceof LivingEntity entity) {
            blessedDamageMod += (float) entity.getAttributeValue(BLEntityAttributes.BLESSED_DAMAGE);
        }
        if (VampireHelper.isVampire(this)) {
//            VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(this);
//            if (!VampireComponent.isEffectiveAgainstVampires(source.get()) && vampire.getAbilties().hasAbility(BLVampireAbilities.DAMAGE_REDUCTION))
//                amount *= 0.85f;
            return blessedDamageMod + VampireComponent.calculateDamage(amount, source.get());
        }
        return blessedDamageMod + amount;
    }

    @Inject(method = "applyDamage", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    public void sanguinisluxuria$cancelBloodDrainOnDamageTaken(DamageSource source, float amount, CallbackInfo ci) {
        if (!VampireHelper.isVampire((LivingEntity) (Object) this))
            return;

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(this);
        vampire.stopSuckingBlood();
    }

    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tryUseTotem(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    public boolean sanguinisluxuria$tryPreventDeath(LivingEntity instance, DamageSource source, Operation<Boolean> original) {
        if (original.call(instance, source))
            return true;

        if (!VampireHelper.isVampire(instance))
            return false;

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(instance);
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(instance);

        if (blood.getBlood() == 0 || VampireComponent.isEffectiveAgainstVampires(source) || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY))
            return false;

        instance.setHealth(Math.min(instance.getMaxHealth(), (float) blood.getBlood()));
        vampire.setDowned(true);
        blood.setBlood(0);
        return true;
    }

    @Inject(method = "getGroup", at = @At("HEAD"), cancellable = true)
    public void sanguinisluxuria$modifyVampireGroup(CallbackInfoReturnable<EntityGroup> cir) {
        if (VampireHelper.isVampire(this))
            cir.setReturnValue(EntityGroup.UNDEAD);
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void sanguinisluxuria$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
          .add(BLEntityAttributes.BLESSED_DAMAGE)
          .add(BLEntityAttributes.BLINK_RANGE, 8)
          .add(BLEntityAttributes.BLINK_COOLDOWN, 250);
    }
}
