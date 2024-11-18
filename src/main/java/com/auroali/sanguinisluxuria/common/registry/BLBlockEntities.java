package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.blockentities.AltarBlockEntity;
import com.auroali.sanguinisluxuria.common.blockentities.PedestalBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BLBlockEntities {
    public static final BlockEntityType<AltarBlockEntity> SKILL_UPGRADER = BlockEntityType.Builder.create(
      AltarBlockEntity::new, BLBlocks.ALTAR
    ).build(null);
    public static final BlockEntityType<PedestalBlockEntity> PEDESTAL = BlockEntityType.Builder.create(
      PedestalBlockEntity::new, BLBlocks.PEDESTAL
    ).build(null);

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, BLResources.ALTAR_ID, SKILL_UPGRADER);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, BLResources.PEDESTAL_ID, PEDESTAL);
    }
}
