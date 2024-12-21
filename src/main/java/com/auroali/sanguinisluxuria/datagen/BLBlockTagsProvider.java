package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class BLBlockTagsProvider extends FabricTagProvider<Block> {
    public BLBlockTagsProvider(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataGenerator, RegistryKeys.BLOCK, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup args) {
        this.getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
          .add(BLBlocks.ALTAR)
          .add(BLBlocks.PEDESTAL)
          .add(BLBlocks.SILVER_ORE)
          .add(BLBlocks.DEEPSLATE_SILVER_ORE)
          .add(BLBlocks.SILVER_BLOCK)
          .add(BLBlocks.RAW_SILVER_BLOCK);
        this.getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
          .add(BLBlocks.SILVER_ORE)
          .add(BLBlocks.DEEPSLATE_SILVER_ORE)
          .add(BLBlocks.SILVER_BLOCK)
          .add(BLBlocks.RAW_SILVER_BLOCK);
        this.getOrCreateTagBuilder(BlockTags.CAULDRONS)
          .add(BLBlocks.BLOOD_CAULDRON);
        this.getOrCreateTagBuilder(ConventionalBlockTags.ORES)
          .add(BLBlocks.SILVER_ORE)
          .add(BLBlocks.DEEPSLATE_SILVER_ORE);
        this.getOrCreateTagBuilder(BlockTags.ALL_SIGNS)
          .add(BLBlocks.DECAYED_SIGN)
          .add(BLBlocks.DECAYED_WALL_SIGN);
        this.getOrCreateTagBuilder(BlockTags.ALL_HANGING_SIGNS)
          .add(BLBlocks.DECAYED_HANGING_SIGN)
          .add(BLBlocks.DECAYED_WALL_HANGING_SIGN);
        this.getOrCreateTagBuilder(BlockTags.PLANKS)
          .add(BLBlocks.DECAYED_PLANKS);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_SLABS)
          .add(BLBlocks.DECAYED_SLAB);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_DOORS)
          .add(BLBlocks.DECAYED_DOOR);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_TRAPDOORS)
          .add(BLBlocks.DECAYED_TRAPDOOR);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS)
          .add(BLBlocks.DECAYED_BUTTON);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_FENCES)
          .add(BLBlocks.DECAYED_FENCE);
        this.getOrCreateTagBuilder(BlockTags.FENCE_GATES)
          .add(BLBlocks.DECAYED_FENCE_GATE);
        this.getOrCreateTagBuilder(BLTags.Blocks.DECAYED_LOGS)
          .add(BLBlocks.DECAYED_WOOD)
          .add(BLBlocks.DECAYED_LOG)
          .add(BLBlocks.STRIPPED_DECAYED_LOG)
          .add(BLBlocks.STRIPPED_DECAYED_WOOD)
          .add(BLBlocks.HUNGRY_DECAYED_LOG)
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES)
          .add(BLBlocks.DECAYED_PRESSURE_PLATE);
        this.getOrCreateTagBuilder(BlockTags.LOGS)
          .add(BLBlocks.DECAYED_WOOD)
          .add(BLBlocks.DECAYED_LOG)
          .add(BLBlocks.STRIPPED_DECAYED_LOG)
          .add(BLBlocks.STRIPPED_DECAYED_WOOD)
          .add(BLBlocks.HUNGRY_DECAYED_LOG)
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG);
        this.getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN)
          .add(BLBlocks.DECAYED_WOOD)
          .add(BLBlocks.DECAYED_LOG)
          .add(BLBlocks.STRIPPED_DECAYED_LOG)
          .add(BLBlocks.STRIPPED_DECAYED_WOOD)
          .add(BLBlocks.HUNGRY_DECAYED_LOG)
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG);
        this.getOrCreateTagBuilder(BLTags.Blocks.HUNGRY_DECAYED_LOGS)
          .add(BLBlocks.HUNGRY_DECAYED_LOG)
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG);
        this.getOrCreateTagBuilder(BlockTags.PRESSURE_PLATES)
          .add(BLBlocks.SILVER_PRESSURE_PLATE)
          .add(BLBlocks.DECAYED_PRESSURE_PLATE);
        this.getOrCreateTagBuilder(BLTags.Blocks.SILVER_BLOCKS)
          .add(BLBlocks.SILVER_BLOCK);
        this.getOrCreateTagBuilder(BLTags.Blocks.RAW_SILVER_BLOCKS)
          .add(BLBlocks.RAW_SILVER_BLOCK);
        this.getOrCreateTagBuilder(BLTags.Blocks.SILVER_ORES)
          .add(BLBlocks.SILVER_ORE)
          .add(BLBlocks.DEEPSLATE_SILVER_ORE);
        this.getOrCreateTagBuilder(BlockTags.SAPLINGS)
          .add(BLBlocks.GRAFTED_SAPLING);
        this.getOrCreateTagBuilder(BLTags.Blocks.NO_MIST_COLLISION)
          .add(Blocks.IRON_BARS)
          .forceAddTag(BlockTags.DOORS)
          .forceAddTag(BlockTags.TRAPDOORS)
          .forceAddTag(BlockTags.FENCE_GATES)
          .forceAddTag(BlockTags.FENCES);
    }
}
