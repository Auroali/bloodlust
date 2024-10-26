package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.advancements.BecomeVampireCriterion;
import com.auroali.sanguinisluxuria.common.advancements.UnlockAbilityCriterion;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.registry.*;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.data.server.recipe.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.ItemTags;

import java.util.function.Consumer;

public class BLRecipeProvider extends FabricRecipeProvider {
    public BLRecipeProvider(FabricDataOutput dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(BLTags.Items.DECAYED_LOGS), RecipeCategory.TOOLS, BLItems.MASK_1)
          .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
          .offerTo(exporter);
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(BLTags.Items.DECAYED_LOGS), RecipeCategory.TOOLS, BLItems.MASK_2)
          .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
          .offerTo(exporter);
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(BLTags.Items.DECAYED_LOGS), RecipeCategory.TOOLS, BLItems.MASK_3)
          .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
          .offerTo(exporter);
        AltarRecipeJsonBuilder.create(RecipeCategory.BREWING, BLItems.TWISTED_BLOOD)
          .input(BloodStorageItem.setStoredBlood(new ItemStack(BLItems.BLOOD_BOTTLE), BloodConstants.BLOOD_PER_BOTTLE))
          .input(Items.NETHER_WART)
          .input(Items.FERMENTED_SPIDER_EYE)
          .input(BLItems.BLOOD_PETAL)
          .criterion("is_vampire", BecomeVampireCriterion.Conditions.create())
          .offerTo(exporter);
        AltarRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.BLOOD_BAG)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.GLASS_BOTTLE)
          .input(Items.GLASS)
          .input(Items.GLASS)
          .criterion("has_twisted_blood", conditionsFromItem(BLItems.TWISTED_BLOOD))
          .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, BLBlocks.ALTAR)
          .pattern("lbl")
          .pattern("sss")
          .input('b', BLItems.BLOOD_BOTTLE)
          .input('s', Items.BLACKSTONE)
          .input('l', BLTags.Items.DECAYED_LOGS)
          .criterion("is_vampire", BecomeVampireCriterion.Conditions.create())
          .criterion("has_blackstone", conditionsFromItem(Items.BLACKSTONE))
          .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, BLBlocks.PEDESTAL)
          .pattern(" l ")
          .pattern(" s ")
          .input('s', Items.BLACKSTONE_WALL)
          .input('l', BLTags.Items.DECAYED_LOGS)
          .criterion("has_blackstone", conditionsFromItem(Items.BLACKSTONE))
          .offerTo(exporter);
        AltarRecipeJsonBuilder.create(RecipeCategory.BREWING, BLItems.BLESSED_BLOOD)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.GOLDEN_APPLE)
          .input(Items.SUNFLOWER)
          .input(PotionUtil.setPotion(new ItemStack(Items.POTION), BLStatusEffects.BLESSED_WATER_POTION))
          .criterion("drink_twisted_blood", ConsumeItemCriterion.Conditions.item(BLItems.TWISTED_BLOOD))
          .offerTo(exporter);
        AltarRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.PENDANT_OF_PIERCING)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.ARROW)
          .input(Items.GOLD_INGOT)
          .input(Items.STRING)
          .criterion("unlock_abilities", UnlockAbilityCriterion.Conditions.create(BLVampireAbilities.TELEPORT))
          .offerTo(exporter);
        AltarRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.PENDANT_OF_TRANSFUSION)
          .input(Items.GLASS_BOTTLE)
          .input(Items.GLASS_BOTTLE)
          .input(Items.IRON_INGOT)
          .input(Items.STRING)
          .criterion("drink_twisted_blood", ConsumeItemCriterion.Conditions.item(BLItems.TWISTED_BLOOD))
          .offerTo(exporter);
        BloodCauldronRecipeJsonBuilder.create(RecipeCategory.BREWING, Ingredient.fromTag(ItemTags.FLOWERS), BLItems.BLOOD_PETAL)
          .criterion("become_vampire", BecomeVampireCriterion.Conditions.create())
          .offerTo(exporter);
        CookingRecipeJsonBuilder.createSmelting(Ingredient.ofItems(BLItems.RAW_SILVER), RecipeCategory.MISC, BLItems.SILVER_INGOT, 0.35f, 200)
          .criterion("has_item", conditionsFromItem(BLItems.RAW_SILVER))
          .offerTo(exporter, BLResources.id("smelting/silver_ingot"));
        CookingRecipeJsonBuilder.createBlasting(Ingredient.ofItems(BLItems.RAW_SILVER), RecipeCategory.MISC, BLItems.SILVER_INGOT, 0.35f, 100)
          .criterion("has_item", conditionsFromItem(BLItems.RAW_SILVER))
          .offerTo(exporter, BLResources.id("blasting/silver_ingot"));
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.SILVER_SWORD)
          .pattern("I")
          .pattern("I")
          .pattern("S")
          .input('I', BLTags.Items.SILVER_INGOTS)
          .input('S', Items.STICK)
          .criterion("has_item", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
          .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.SILVER_PICKAXE)
          .pattern("III")
          .pattern(" S ")
          .pattern(" S ")
          .input('I', BLTags.Items.SILVER_INGOTS)
          .input('S', Items.STICK)
          .criterion("has_item", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
          .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.SILVER_AXE)
          .pattern("II")
          .pattern("IS")
          .pattern(" S")
          .input('I', BLTags.Items.SILVER_INGOTS)
          .input('S', Items.STICK)
          .criterion("has_item", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
          .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.SILVER_SHOVEL)
          .pattern("I")
          .pattern("S")
          .pattern("S")
          .input('I', BLTags.Items.SILVER_INGOTS)
          .input('S', Items.STICK)
          .criterion("has_item", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
          .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, BLItems.SILVER_HOE)
          .pattern("II")
          .pattern(" S")
          .pattern(" S")
          .input('I', BLTags.Items.SILVER_INGOTS)
          .input('S', Items.STICK)
          .criterion("has_item", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
          .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, BLItems.SILVER_INGOT, 9)
          .input(BLBlocks.SILVER_BLOCK)
          .criterion("has_silver", conditionsFromItem(BLBlocks.SILVER_BLOCK))
          .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, BLBlocks.SILVER_BLOCK)
          .input(BLItems.SILVER_INGOT, 9)
          .criterion("has_silver", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
          .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, BLItems.RAW_SILVER, 9)
          .input(BLBlocks.RAW_SILVER_BLOCK)
          .criterion("has_silver", conditionsFromItem(BLItems.RAW_SILVER))
          .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, BLBlocks.RAW_SILVER_BLOCK)
          .input(BLItems.RAW_SILVER, 9)
          .criterion("has_silver", conditionsFromItem(BLItems.RAW_SILVER))
          .offerTo(exporter);
        BloodCauldronFillRecipeJsonBuilder.create(RecipeCategory.BREWING, Ingredient.ofItems(Items.GLASS_BOTTLE), BLItems.BLOOD_BOTTLE)
          .criterion("has_item", conditionsFromItem(BLItems.BLOOD_BOTTLE))
          .offerTo(exporter);
        BloodCauldronFillRecipeJsonBuilder.create(RecipeCategory.BREWING, BLItems.BLOOD_BAG)
          .criterion("has_item", conditionsFromItem(BLItems.BLOOD_BAG))
          .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, BLBlocks.GRAFTED_SAPLING)
          .input(BLItems.BLOOD_PETAL)
          .input(ItemTags.SAPLINGS)
          .criterion(hasItem(BLItems.BLOOD_PETAL), conditionsFromItem(BLItems.BLOOD_PETAL))
          .offerTo(exporter);
        // i dont want to make planks ;-;
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BLBlocks.DECAYED_PRESSURE_PLATE)
          .pattern("##")
          .input('#', BLTags.Items.DECAYED_LOGS)
          .criterion("has_item", conditionsFromTag(BLTags.Items.DECAYED_LOGS))
          .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BLBlocks.SILVER_PRESSURE_PLATE)
          .pattern("##")
          .input('#', BLTags.Items.SILVER_INGOTS)
          .criterion("has_item", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
          .offerTo(exporter);
    }
}
