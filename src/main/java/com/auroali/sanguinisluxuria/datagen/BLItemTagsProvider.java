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
        this.getOrCreateTagBuilder(BLTags.Items.FACE_TRINKETS)
          .add(BLItems.MASK_1)
          .add(BLItems.MASK_2)
          .add(BLItems.MASK_3);
        this.getOrCreateTagBuilder(BLTags.Items.NECKLACE_TRINKETS)
          .add(BLItems.PENDANT_OF_PIERCING);
        this.getOrCreateTagBuilder(BLTags.Items.SUN_BLOCKING_HELMETS)
          .add(Items.LEATHER_HELMET)
          .add(Items.CARVED_PUMPKIN);
        this.getOrCreateTagBuilder(BLTags.Items.VAMPIRE_MASKS)
          .add(BLItems.MASK_1)
          .add(BLItems.MASK_2)
          .add(BLItems.MASK_3);
        this.getOrCreateTagBuilder(BLTags.Items.SILVER_INGOTS)
          .add(BLItems.SILVER_INGOT);
        this.getOrCreateTagBuilder(ItemTags.PICKAXES)
          .add(BLItems.SILVER_PICKAXE);
        this.getOrCreateTagBuilder(ItemTags.AXES)
          .add(BLItems.SILVER_AXE);
        this.getOrCreateTagBuilder(ItemTags.SWORDS)
          .add(BLItems.SILVER_SWORD);
        this.getOrCreateTagBuilder(ItemTags.HOES)
          .add(BLItems.SILVER_HOE);
        this.getOrCreateTagBuilder(ItemTags.SHOVELS)
          .add(BLItems.SILVER_SHOVEL);
        this.getOrCreateTagBuilder(ConventionalItemTags.ORES)
          .add(BLBlocks.SILVER_ORE.asItem())
          .add(BLBlocks.DEEPSLATE_SILVER_ORE.asItem());
        this.getOrCreateTagBuilder(BLTags.Items.DECAYED_LOGS)
          .add(BLBlocks.DECAYED_WOOD.asItem())
          .add(BLBlocks.DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_WOOD.asItem())
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        this.getOrCreateTagBuilder(ItemTags.LOGS)
          .add(BLBlocks.DECAYED_WOOD.asItem())
          .add(BLBlocks.DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_WOOD.asItem())
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        this.getOrCreateTagBuilder(ItemTags.LOGS_THAT_BURN)
          .add(BLBlocks.DECAYED_WOOD.asItem())
          .add(BLBlocks.DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_DECAYED_WOOD.asItem())
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        this.getOrCreateTagBuilder(BLTags.Items.HUNGRY_DECAYED_LOGS)
          .add(BLBlocks.HUNGRY_DECAYED_LOG.asItem())
          .add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG.asItem());
        this.getOrCreateTagBuilder(ItemTags.WOODEN_PRESSURE_PLATES)
          .add(BLBlocks.DECAYED_PRESSURE_PLATE.asItem());
        this.getOrCreateTagBuilder(BLTags.Items.SILVER_BLOCKS)
          .add(BLBlocks.SILVER_BLOCK.asItem());
        this.getOrCreateTagBuilder(BLTags.Items.RAW_SILVER_BLOCKS)
          .add(BLBlocks.RAW_SILVER_BLOCK.asItem());
        this.getOrCreateTagBuilder(BLTags.Items.SILVER_ORES)
          .add(BLBlocks.SILVER_ORE.asItem())
          .add(BLBlocks.DEEPSLATE_SILVER_ORE.asItem());
        this.getOrCreateTagBuilder(ConventionalItemTags.INGOTS)
          .add(BLItems.SILVER_INGOT);
        this.getOrCreateTagBuilder(ItemTags.SAPLINGS)
          .add(BLBlocks.GRAFTED_SAPLING.asItem());

        this.getOrCreateTagBuilder(ItemTags.PLANKS)
          .add(BLBlocks.DECAYED_PLANKS.asItem());
        this.getOrCreateTagBuilder(ItemTags.WOODEN_FENCES)
          .add(BLBlocks.DECAYED_FENCE.asItem());
        this.getOrCreateTagBuilder(ItemTags.FENCE_GATES)
          .add(BLBlocks.DECAYED_FENCE_GATE.asItem());
        this.getOrCreateTagBuilder(ItemTags.WOODEN_DOORS)
          .add(BLBlocks.DECAYED_DOOR.asItem());
        this.getOrCreateTagBuilder(ItemTags.WOODEN_BUTTONS)
          .add(BLBlocks.DECAYED_BUTTON.asItem());
        this.getOrCreateTagBuilder(ItemTags.WOODEN_TRAPDOORS)
          .add(BLBlocks.DECAYED_TRAPDOOR.asItem());
        this.getOrCreateTagBuilder(ItemTags.WOODEN_STAIRS)
          .add(BLBlocks.DECAYED_STAIRS.asItem());
        this.getOrCreateTagBuilder(ItemTags.WOODEN_SLABS)
          .add(BLBlocks.DECAYED_SLAB.asItem());

        this.getOrCreateTagBuilder(BLTags.Items.BLOOD_STORING_BOTTLES)
          .add(Items.GLASS_BOTTLE);
    }
}
