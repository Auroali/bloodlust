package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class BLItemGroups {
    public static final RegistryKey<ItemGroup> SANGUINIS_LUXURIA_TAB = RegistryKey.of(RegistryKeys.ITEM_GROUP, BLResources.ITEM_GROUP_ID);

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, BLResources.ITEM_GROUP_ID, FabricItemGroup.builder()
          .icon(() -> BloodStorageItem.createStack(BLItems.BLOOD_BOTTLE))
          .displayName(Text.translatable("itemGroup.sanguinisluxuria.sanguinisluxuria"))
          .entries((displayContext, entries) -> {
              entries.add(BloodStorageItem.createStack(BLItems.BLOOD_BOTTLE));
              entries.add(BLItems.TWISTED_BLOOD);
              entries.add(BloodStorageItem.createStack(BLItems.BLOOD_BAG, 0));
              entries.add(BloodStorageItem.createStack(BLItems.BLOOD_BAG));
              entries.add(BLItems.MASK_1);
              entries.add(BLItems.MASK_2);
              entries.add(BLItems.MASK_3);
              entries.add(BLItems.BLOOD_PETAL);
              entries.add(BLItems.PENDANT_OF_PIERCING);
              entries.add(BLItems.SILVER_SWORD);
              entries.add(BLItems.SILVER_PICKAXE);
              entries.add(BLItems.SILVER_AXE);
              entries.add(BLItems.SILVER_SHOVEL);
              entries.add(BLItems.SILVER_HOE);
              entries.add(BLItems.SILVER_INGOT);
              entries.add(BLBlocks.SILVER_BLOCK);
              entries.add(BLBlocks.SILVER_PRESSURE_PLATE);
              entries.add(BLItems.RAW_SILVER);
              entries.add(BLBlocks.RAW_SILVER_BLOCK);
              entries.add(BLBlocks.SILVER_ORE);
              entries.add(BLBlocks.DEEPSLATE_SILVER_ORE);
              entries.add(BLBlocks.ALTAR);
              entries.add(BLBlocks.PEDESTAL);
              entries.add(BLBlocks.GRAFTED_SAPLING);
              entries.add(BLBlocks.DECAYED_TWIGS);
              entries.add(BLBlocks.HUNGRY_DECAYED_LOG);
              entries.add(BLBlocks.DECAYED_LOG);
              entries.add(BLBlocks.DECAYED_WOOD);
              entries.add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG);
              entries.add(BLBlocks.STRIPPED_DECAYED_LOG);
              entries.add(BLBlocks.STRIPPED_DECAYED_WOOD);
              entries.add(BLBlocks.DECAYED_PLANKS);
              entries.add(BLBlocks.DECAYED_SLAB);
              entries.add(BLBlocks.DECAYED_STAIRS);
              entries.add(BLBlocks.DECAYED_FENCE);
              entries.add(BLBlocks.DECAYED_FENCE_GATE);
              entries.add(BLBlocks.DECAYED_TRAPDOOR);
              entries.add(BLBlocks.DECAYED_DOOR);
              entries.add(BLBlocks.DECAYED_SIGN);
              entries.add(BLBlocks.DECAYED_HANGING_SIGN);
              entries.add(BLBlocks.DECAYED_BUTTON);
              entries.add(BLBlocks.DECAYED_PRESSURE_PLATE);
              entries.add(BLItems.VAMPIRE_VILLAGER_SPAWN_EGG);
              entries.add(PotionUtil.setPotion(new ItemStack(Items.POTION), BLStatusEffects.BLESSED_WATER_POTION));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.POTION), BLStatusEffects.BLESSED_WATER_POTION_TWO));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.POTION), BLStatusEffects.BLOOD_LUST_POTION));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), BLStatusEffects.BLESSED_WATER_POTION));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), BLStatusEffects.BLESSED_WATER_POTION_TWO));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), BLStatusEffects.BLOOD_LUST_POTION));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), BLStatusEffects.BLESSED_WATER_POTION));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), BLStatusEffects.BLESSED_WATER_POTION_TWO));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), BLStatusEffects.BLOOD_LUST_POTION));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW), BLStatusEffects.BLESSED_WATER_POTION));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW), BLStatusEffects.BLESSED_WATER_POTION_TWO));
              entries.add(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW), BLStatusEffects.BLOOD_LUST_POTION));
          })
          .build());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
          .register(entries -> {
              entries.add(BLItems.VAMPIRE_VILLAGER_SPAWN_EGG);
          });
    }
}
