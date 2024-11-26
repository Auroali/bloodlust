package com.auroali.sanguinisluxuria.mixin.client;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "update", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/world/ClientWorld;getSkyBrightness(F)F", shift = At.Shift.AFTER))
    public void sanguinisluxuria$raiseMinimumSkyBrightness(float delta, CallbackInfo ci, @Local(ordinal = 1) LocalFloatRef brightness) {
        if (VampireHelper.isVampire(this.client.player))
            brightness.set(Math.max(brightness.get(), 0.46f));
    }
}
