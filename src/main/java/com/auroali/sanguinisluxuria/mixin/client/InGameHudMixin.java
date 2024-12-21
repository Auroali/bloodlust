package com.auroali.sanguinisluxuria.mixin.client;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private static Identifier ICONS;

    @ModifyArg(method = "renderStatusBars", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
    ), slice = @Slice(from = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
      ordinal = 1), to = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 2
    )))
    public Identifier sanguinisluxuria$injectHungerIcons(Identifier texture) {
        if (VampireHelper.isVampire(MinecraftClient.getInstance().player)) {
            return BLResources.ICONS;
        }
        return texture;
    }

    @ModifyVariable(method = "renderVignetteOverlay", at = @At(
      value = "STORE"
    ), ordinal = 1)
    public float sanguinisluxuria$showSunTimeProgress(float g) {
        PlayerEntity entity = MinecraftClient.getInstance().player;
        if (VampireHelper.isVampire(entity)) {
            VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(entity);
            if (vampire.getTimeInSun() == 0)
                return g;
            return MathHelper.clamp(vampire.getTimeInSun() / (float) vampire.getMaxTimeInSun(), 0.f, 1.f);
        }
        return g;
    }

    @Inject(method = "renderVignetteOverlay", at = @At(
      value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;setShaderColor(FFFF)V", ordinal = 1, shift = At.Shift.AFTER))
    public void sanguinisluxuria$modifyVignetteColourInSun(DrawContext context, Entity entity, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (VampireHelper.isVampire(player)) {
            VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(player);
            if (vampire.getTimeInSun() != 0) {
                float multiplier = MathHelper.clamp(vampire.getTimeInSun() / (float) vampire.getMaxTimeInSun(), 0.f, 1.f);
                context.setShaderColor(multiplier * 0.6f, multiplier * 0.95f, multiplier, 0.0f);
            }
        }
    }
}
