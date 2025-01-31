package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.blockentities.AltarBlockEntity;
import com.auroali.sanguinisluxuria.common.blockentities.PedestalBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;

public class BLBlockEntities {
    public static final List<Block> SIGNS = List.of(BLBlocks.DECAYED_SIGN, BLBlocks.DECAYED_WALL_SIGN);
    public static final List<Block> HANGING_SIGNS = List.of(BLBlocks.DECAYED_HANGING_SIGN, BLBlocks.DECAYED_WALL_HANGING_SIGN);

    public static final BlockEntityType<AltarBlockEntity> ALTAR = BlockEntityType.Builder.create(
      AltarBlockEntity::new, BLBlocks.ALTAR
    ).build(null);
    public static final BlockEntityType<PedestalBlockEntity> PEDESTAL = BlockEntityType.Builder.create(
      PedestalBlockEntity::new, BLBlocks.PEDESTAL
    ).build(null);

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, BLResources.ALTAR_ID, ALTAR);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, BLResources.PEDESTAL_ID, PEDESTAL);
    }
}
