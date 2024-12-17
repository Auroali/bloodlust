package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.common.registry.BLEntityBloodDrainEffects;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.DataPackContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataPackContents.class)
public class DatapackContentsMixin {
    @Inject(method = "refresh", at = @At("RETURN"))
    public void sanguinisluxuria$flushEffectCache(DynamicRegistryManager dynamicRegistryManager, CallbackInfo ci) {
        BLEntityBloodDrainEffects.flushCache();
    }
}
