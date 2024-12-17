package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;

public class BLBlockFamilies {
    public static final BlockFamily DECAYED_WOOD_FAMILY = BlockFamilies.register(BLBlocks.DECAYED_PLANKS)
      .sign(BLBlocks.DECAYED_SIGN, BLBlocks.DECAYED_WALL_SIGN)
      .pressurePlate(BLBlocks.DECAYED_PRESSURE_PLATE)
      .fence(BLBlocks.DECAYED_FENCE)
      .fenceGate(BLBlocks.DECAYED_FENCE_GATE)
      .slab(BLBlocks.DECAYED_SLAB)
      .stairs(BLBlocks.DECAYED_STAIRS)
      .button(BLBlocks.DECAYED_BUTTON)
      .door(BLBlocks.DECAYED_DOOR)
      .trapdoor(BLBlocks.DECAYED_TRAPDOOR)
      .build();
}
