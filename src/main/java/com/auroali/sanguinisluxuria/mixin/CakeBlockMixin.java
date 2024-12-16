package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.VampireHungerManager;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {
    @WrapOperation(method = "tryEat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
    private static void sanguinisluxuria$preventCakeEat(HungerManager instance, int food, float saturationModifier, Operation<Void> original, @Local(argsOnly = true) PlayerEntity player, @Local(argsOnly = true) BlockState state) {
        if (!VampireHelper.isVampire(player)) {
            original.call(instance, food, saturationModifier);
        }

        Item item = state.getBlock().asItem();
        if (item != null && item.getRegistryEntry().isIn(BLTags.Items.VAMPIRES_GET_HUNGER_FROM))
            ((VampireHungerManager) instance).sanguinisluxuria$addHunger(food, saturationModifier);
    }
}
