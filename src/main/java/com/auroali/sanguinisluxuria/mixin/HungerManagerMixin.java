package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.VampireHungerManager;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.auroali.sanguinisluxuria.config.BLConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin implements VampireHungerManager {
    @Shadow
    private int foodLevel;
    @Shadow
    private float saturationLevel;
    @Unique
    public PlayerEntity sanguinisluxuria$hmTrackedPlayer = null;

    @Inject(method = "update", at = @At("HEAD"))
    public void sanguinisluxuria$setTrackedPlayer(PlayerEntity player, CallbackInfo ci) {
        this.sanguinisluxuria$hmTrackedPlayer = player;
    }

    @ModifyConstant(method = "update",
      slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getHealth()F", ordinal = 1)),
      constant = @Constant(floatValue = 1.0f, ordinal = 1)
    )
    public float sanguinisluxuria$stopStarvingDamageWhenDowned(float constant) {
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer) && BLEntityComponents.VAMPIRE_COMPONENT.get(this.sanguinisluxuria$hmTrackedPlayer).isDown()) {
            return 0;
        }
        return constant;
    }

    @ModifyVariable(method = "addExhaustion", at = @At("HEAD"), argsOnly = true)
    public float sanguinisluxuria$lowerExhaustionForVampires(float exhaustion) {
        // if the player is a vampire and does not have hunger, reduce exhaustion
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer) && !this.sanguinisluxuria$hmTrackedPlayer.hasStatusEffect(StatusEffects.HUNGER))
            return exhaustion * BLConfig.INSTANCE.vampireExhaustionMultiplier;

        return exhaustion;
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 10))
    public int sanguinisluxuria$modifyHealRate(int constant) {
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer) && !this.sanguinisluxuria$hmTrackedPlayer.isOnFire())
            return constant / 4;
        return constant;
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 80, ordinal = 0))
    public int sanguinisluxuria$modifySecondHealRate(int constant) {
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer) && !this.sanguinisluxuria$hmTrackedPlayer.isOnFire())
            return constant / 4;
        return constant;
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 18))
    public int sanguinisluxuria$modifyMinFoodLevelForRegen(int constant) {
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer))
            return 8;
        return constant;
    }

    @ModifyConstant(
      method = "update",
      constant = @Constant(floatValue = 6.0f, ordinal = 2)
    )
    public float sanguinisluxuria$makeRegenUseMoreExhaustion(float value) {
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer))
            return 2.5f / (BLConfig.INSTANCE.vampireExhaustionMultiplier);
        return value;
    }

    @WrapOperation(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
    public void sanguinisluxuria$handleVampireEdibleFood(HungerManager instance, int food, float saturationModifier, Operation<Void> original, @Local(argsOnly = true) ItemStack stack) {
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer) && stack.isIn(BLTags.Items.VAMPIRES_GET_HUNGER_FROM))
            this.sanguinisluxuria$addHunger(food, saturationModifier);
        else
            original.call(instance, food, saturationModifier);
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    public void sanguinisluxuria$cancelAddIfVampire(int food, float saturationModifier, CallbackInfo ci) {
        if (VampireHelper.isVampire(this.sanguinisluxuria$hmTrackedPlayer))
            ci.cancel();
    }

    @Unique
    @Override
    public void sanguinisluxuria$setPlayer(PlayerEntity player) {
        this.sanguinisluxuria$hmTrackedPlayer = player;
    }

    @Unique
    @Override
    public void sanguinisluxuria$addHunger(int food, float saturationModifier) {
        this.foodLevel = Math.min(food + this.foodLevel, 20);
        this.saturationLevel = Math.min(this.saturationLevel + (float) food * saturationModifier * 2.0F, (float) this.foodLevel);
    }
}
