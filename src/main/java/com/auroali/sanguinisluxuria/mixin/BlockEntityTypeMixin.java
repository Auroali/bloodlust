package com.auroali.sanguinisluxuria.mixin;

import com.auroali.sanguinisluxuria.common.registry.BLBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    // my ide is complaining but it compiles so i think it just doesnt know how to handle this
    @ModifyArg(method = "<clinit>", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/block/entity/BlockEntityType$Builder;create(Lnet/minecraft/block/entity/BlockEntityType$BlockEntityFactory;[Lnet/minecraft/block/Block;)Lnet/minecraft/block/entity/BlockEntityType$Builder;",
      ordinal = 7
    ), index = 1)
    private static Block[] sanguinisluxuria$modifySignBlocks(Block[] original) {
        List<Block> newBlocks = new ArrayList<>(Arrays.asList(original));
        newBlocks.addAll(BLBlockEntities.SIGNS);
        return newBlocks.toArray(new Block[0]);
    }

    @ModifyArg(method = "<clinit>", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/block/entity/BlockEntityType$Builder;create(Lnet/minecraft/block/entity/BlockEntityType$BlockEntityFactory;[Lnet/minecraft/block/Block;)Lnet/minecraft/block/entity/BlockEntityType$Builder;",
      ordinal = 8
    ), index = 1)
    private static Block[] sanguinisluxuria$modifyHangingSignBlocks(Block[] original) {
        List<Block> newBlocks = new ArrayList<>(Arrays.asList(original));
        newBlocks.addAll(BLBlockEntities.HANGING_SIGNS);
        return newBlocks.toArray(new Block[0]);
    }
}
