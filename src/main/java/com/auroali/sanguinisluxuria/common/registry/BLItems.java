package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.items.*;
import com.auroali.sanguinisluxuria.common.items.tools.*;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BLItems {
    public static final MaskItem MASK_1 = new MaskItem(new Item.Settings().maxCount(1));
    public static final MaskItem MASK_2 = new MaskItem(new Item.Settings().maxCount(1));
    public static final MaskItem MASK_3 = new MaskItem(new Item.Settings().maxCount(1));
    public static final BloodStorageItem BLOOD_BAG = new DrinkableBloodStorageItem(
      new Item.Settings().maxCount(1),
      10 * BloodConstants.BLOOD_PER_BOTTLE
    );
    public static final BloodStorageItem BLOOD_BOTTLE = new BloodBottleItem(
      new Item.Settings().maxCount(1).recipeRemainder(Items.GLASS_BOTTLE),
      BloodConstants.BLOOD_PER_BOTTLE
    ).emptyItem(Items.GLASS_BOTTLE);
    public static final Item TWISTED_BLOOD = new TwistedBloodItem(new Item.Settings().maxCount(1));
    public static final Item VAMPIRE_VILLAGER_SPAWN_EGG = new SpawnEggItem(BLEntities.VAMPIRE_VILLAGER, 0xFF1E1C1B, 0xFFF9f8EF, new Item.Settings());
    public static final Item PENDANT_OF_PIERCING = new PendantOfPiercingItem(new Item.Settings().maxCount(1));
    public static final Item BLOOD_PETAL = new Item(new Item.Settings());
    public static final Item SILVER_INGOT = new Item(new Item.Settings());
    public static final Item RAW_SILVER = new Item(new Item.Settings());
    public static final Item SILVER_SWORD = new BlessedSwordItem(BLToolMaterials.SILVER, 3, -2.4f, 4f, new Item.Settings());
    public static final Item SILVER_PICKAXE = new BlessedPickaxeItem(BLToolMaterials.SILVER, 1, -2.8f, 2, new Item.Settings());
    public static final Item SILVER_AXE = new BlessedAxeItem(BLToolMaterials.SILVER, 6, -3.1f, 2.5f, new Item.Settings());
    public static final Item SILVER_SHOVEL = new BlessedShovelItem(BLToolMaterials.SILVER, 1.5f, -3.f, 2, new Item.Settings());
    public static final Item SILVER_HOE = new BlessedHoeItem(BLToolMaterials.SILVER, 0, -1.f, 2, new Item.Settings());

    public static void register() {
        Registry.register(Registries.ITEM, BLResources.MASK_ONE_ID, MASK_1);
        Registry.register(Registries.ITEM, BLResources.MASK_TWO_ID, MASK_2);
        Registry.register(Registries.ITEM, BLResources.MASK_THREE_ID, MASK_3);
        Registry.register(Registries.ITEM, BLResources.BLOOD_BAG_ID, BLOOD_BAG);
        Registry.register(Registries.ITEM, BLResources.BLOOD_BOTTLE_ID, BLOOD_BOTTLE);
        Registry.register(Registries.ITEM, BLResources.TWISTED_BLOOD_ID, TWISTED_BLOOD);
        Registry.register(Registries.ITEM, BLResources.VAMPIRE_VILLAGER_SPAWN_EGG, VAMPIRE_VILLAGER_SPAWN_EGG);
        Registry.register(Registries.ITEM, BLResources.PENDANT_OF_PIERCING, PENDANT_OF_PIERCING);
        Registry.register(Registries.ITEM, BLResources.BLOOD_PETAL_ID, BLOOD_PETAL);
        Registry.register(Registries.ITEM, BLResources.SILVER_INGOT_ID, SILVER_INGOT);
        Registry.register(Registries.ITEM, BLResources.RAW_SILVER_ID, RAW_SILVER);
        Registry.register(Registries.ITEM, BLResources.SILVER_SWORD_ID, SILVER_SWORD);
        Registry.register(Registries.ITEM, BLResources.SILVER_PICKAXE_ID, SILVER_PICKAXE);
        Registry.register(Registries.ITEM, BLResources.SILVER_AXE_ID, SILVER_AXE);
        Registry.register(Registries.ITEM, BLResources.SILVER_SHOVEL_ID, SILVER_SHOVEL);
        Registry.register(Registries.ITEM, BLResources.SILVER_HOE_ID, SILVER_HOE);

        // blocks
        registerBlock(BLResources.ALTAR_ID, BLBlocks.ALTAR);
        registerBlock(BLResources.PEDESTAL_ID, BLBlocks.PEDESTAL);
        registerBlock(BLResources.SILVER_BLOCK_ID, BLBlocks.SILVER_BLOCK);
        registerBlock(BLResources.SILVER_ORE_ID, BLBlocks.SILVER_ORE);
        registerBlock(BLResources.DEEPSLATE_SILVER_ORE_ID, BLBlocks.DEEPSLATE_SILVER_ORE);
        registerBlock(BLResources.RAW_SILVER_BLOCK_ID, BLBlocks.RAW_SILVER_BLOCK);
        registerBlock(BLResources.HUNGRY_DECAYED_LOG, BLBlocks.HUNGRY_DECAYED_LOG);
        registerBlock(BLResources.STRIPPED_HUNGRY_DECAYED_LOG, BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG);
        registerBlock(BLResources.DECAYED_LOG, BLBlocks.DECAYED_LOG);
        registerBlock(BLResources.STRIPPED_DECAYED_LOG, BLBlocks.STRIPPED_DECAYED_LOG);
        registerBlock(BLResources.STRIPPED_DECAYED_WOOD, BLBlocks.STRIPPED_DECAYED_WOOD);
        registerBlock(BLResources.DECAYED_WOOD, BLBlocks.DECAYED_WOOD);
        registerBlock(BLResources.DECAYED_TWIGS, BLBlocks.DECAYED_TWIGS);
        registerBlock(BLResources.GRAFTED_SAPLING, BLBlocks.GRAFTED_SAPLING);
        registerBlock(BLResources.SILVER_PRESSURE_PLATE, BLBlocks.SILVER_PRESSURE_PLATE);
        registerBlock(BLResources.DECAYED_PRESSURE_PLATE, BLBlocks.DECAYED_PRESSURE_PLATE);

        registerBlock(BLResources.DECAYED_PLANKS, BLBlocks.DECAYED_PLANKS);
        registerBlock(BLResources.DECAYED_FENCE, BLBlocks.DECAYED_FENCE);
        registerBlock(BLResources.DECAYED_FENCE_GATE, BLBlocks.DECAYED_FENCE_GATE);
        registerBlock(BLResources.DECAYED_STAIRS, BLBlocks.DECAYED_STAIRS);
        registerBlock(BLResources.DECAYED_SLAB, BLBlocks.DECAYED_SLAB);
        registerBlock(BLResources.DECAYED_BUTTON, BLBlocks.DECAYED_BUTTON);
        registerBlock(BLResources.DECAYED_DOOR, BLBlocks.DECAYED_DOOR);
        registerBlock(BLResources.DECAYED_TRAPDOOR, BLBlocks.DECAYED_TRAPDOOR);
        Registry.register(Registries.ITEM, BLResources.DECAYED_SIGN, new SignItem(new Item.Settings(), BLBlocks.DECAYED_SIGN, BLBlocks.DECAYED_WALL_SIGN));
        Registry.register(Registries.ITEM, BLResources.DECAYED_HANGING_SIGN, new HangingSignItem(BLBlocks.DECAYED_HANGING_SIGN, BLBlocks.DECAYED_WALL_HANGING_SIGN, new Item.Settings()));

        CompostingChanceRegistry.INSTANCE.add(BLBlocks.DECAYED_TWIGS, 0.15f);
        CompostingChanceRegistry.INSTANCE.add(BLBlocks.GRAFTED_SAPLING, 0.3f);
        CompostingChanceRegistry.INSTANCE.add(BLOOD_PETAL, 0.4f);
    }

    public static void registerBlock(Identifier id, Block block, Item.Settings settings) {
        Registry.register(Registries.ITEM, id, new BlockItem(block, settings));
    }

    public static void registerBlock(Identifier id, Block block) {
        registerBlock(id, block, new Item.Settings());
    }
}
