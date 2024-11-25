package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class BLItemTagsProvider extends FabricTagProvider<Item> {
    public BLItemTagsProvider(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataGenerator, RegistryKeys.ITEM, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(BLTags.Items.FACE_TRINKETS)
          .add(BLItems.MASK_1)
          .add(BLItems.MASK_2)
          .add(BLItems.MASK_3);
        getOrCreateTagBuilder(BLTags.Items.NECKLACE_TRINKETS)
          .add(BLItems.PENDANT_OF_PIERCING);
//        getOrCreateTagBuilder(BLTags.Items.VAMPIRE_EDIBLE)
//                .add(Items.GOLDEN_APPLE)
//                .add(Items.ENCHANTED_GOLDEN_APPLE)
//                .add(Items.HONEY_BOTTLE)
//                .add(Items.SUSPICIOUS_STEW);
        getOrCreateTagBuilder(BLTags.Items.SUN_BLOCKING_HELMETS)
          .add(Items.LEATHER_HELMET)
          .add(Items.CARVED_PUMPKIN);
        getOrCreateTagBuilder(BLTags.Items.VAMPIRE_MASKS)
          .add(BLItems.MASK_1)
          .add(BLItems.MASK_2)
          .add(BLItems.MASK_3);
        getOrCreateTagBuilder(BLTags.Items.SILVER_INGOTS)
          .add(BLItems.SILVER_INGOT);
        getOrCreateTagBuilder(ItemTags.PICKAXES)
          .add(BLItems.SILVER_PICKAXE);
        getOrCreateTagBuilder(ItemTags.AXES)
          .add(BLItems.SILVER_AXE);
        getOrCreateTagBuilder(ItemTags.SWORDS)
          .add(BLItems.SILVER_SWORD);
        getOrCreateTagBuilder(ItemTags.HOES)
          .add(BLItems.SILVER_HOE);
        getOrCreateTagBuilder(ItemTags.SHOVELS)
          .add(BLItems.SILVER_SHOVEL);
        getOrCreateTagBuilder(ConventionalItemTags.ORES)
          .add(BLBlocks.SILVER_ORE.asItem())
          .add(BLBlocks.DEEPSLATE_SILVER_ORE.asItem());
        getOrCreateTagBuilder(BLTags.Items.DECAYED_LOGS)
          .add(BLBlocks.DECAYED_WOOD.asItem())
          .add(BLBlocks.DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_WOOD.asItem())
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        getOrCreateTagBuilder(ItemTags.LOGS)
          .add(BLBlocks.DECAYED_WOOD.asItem())
          .add(BLBlocks.DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_WOOD.asItem())
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        getOrCreateTagBuilder(ItemTags.LOGS_THAT_BURN)
          .add(BLBlocks.DECAYED_WOOD.asItem())
          .add(BLBlocks.DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_WOOD.asItem())
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        getOrCreateTagBuilder(BLTags.Items.HUNGRY_DECAYED_LOGS)
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        getOrCreateTagBuilder(ItemTags.WOODEN_PRESSURE_PLATES)
          .add(BLBlocks.DECAYED_PRESSURE_PLATE.asItem());
        getOrCreateTagBuilder(BLTags.Items.SILVER_BLOCKS)
          .add(BLBlocks.SILVER_BLOCK.asItem());
        getOrCreateTagBuilder(BLTags.Items.RAW_SILVER_BLOCKS)
          .add(BLBlocks.RAW_SILVER_BLOCK.asItem());
        getOrCreateTagBuilder(BLTags.Items.SILVER_ORES)
          .add(BLBlocks.SILVER_ORE.asItem())
          .add(BLBlocks.DEEPSLATE_SILVER_ORE.asItem());
        getOrCreateTagBuilder(ConventionalItemTags.INGOTS)
          .add(BLItems.SILVER_INGOT);
        getOrCreateTagBuilder(ItemTags.SAPLINGS)
          .add(BLBlocks.GRAFTED_SAPLING.asItem());
    }
}
