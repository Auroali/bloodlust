package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.VampireHungerManager;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    protected HungerManager hungerManager;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    public void sanguinisluxuria$forceCrawlingWhenNoBlood(CallbackInfo ci) {
        if (!VampireHelper.isVampire(this))
            return;

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(this);
        if (!this.hasVehicle() && vampire.isDown()) {
            this.setPose(EntityPose.SWIMMING);
            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void sanguinisluxuria$setVampireHungerManagerPlayer(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        ((VampireHungerManager) this.hungerManager).sanguinisluxuria$setPlayer((PlayerEntity) (Object) this);
    }
}
