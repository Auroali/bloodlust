package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.config.BLConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {
    @Inject(method = "applyUpdateEffect", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V",
            shift = At.Shift.BEFORE
    ), cancellable = true)
    public void sanguinisluxuria$preventSaturationForVampires(LivingEntity entity, int amplifier, CallbackInfo ci) {
        if(VampireHelper.isVampire(entity))
            ci.cancel();
    }

    @Inject(method = "applyInstantEffect", at = @At("HEAD"), cancellable = true)
    public void sanguinisluxuria$preventEffectsForVampires(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity, CallbackInfo ci) {
        if(VampireHelper.isVampire(target)) {
            StatusEffect effect = (StatusEffect) (Object) this;
            if(effect == StatusEffects.INSTANT_HEALTH || effect == StatusEffects.INSTANT_DAMAGE)
                ci.cancel();
        }
    }

    @Inject(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", ordinal = 0), cancellable = true)
    public void sanguinisluxuria$preventPoisonFromKilling(LivingEntity entity, int amplifier, CallbackInfo ci) {
        if(VampireHelper.isVampire(entity)) {
            entity.damage(DamageSource.MAGIC, 1.0F / BLConfig.INSTANCE.vampireDamageMultiplier);
            ci.cancel();
        }
    }
}
