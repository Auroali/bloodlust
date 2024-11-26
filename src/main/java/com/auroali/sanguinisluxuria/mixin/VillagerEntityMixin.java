package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.VampireHelper;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {
    @Shadow
    protected abstract void sayNo();

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void sanguinisluxuria$preventUnmaskedVampiresFromTrading(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (VampireHelper.isVampire(player) && !VampireHelper.isMasked(player)) {
            if (hand == Hand.MAIN_HAND && !player.getWorld().isClient)
                this.sayNo();

            player.incrementStat(Stats.TALKED_TO_VILLAGER);
            cir.setReturnValue(ActionResult.success(player.getWorld().isClient));
        }
    }
}
