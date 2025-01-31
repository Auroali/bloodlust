package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.advancements.BecomeVampireCriterion;
import com.auroali.sanguinisluxuria.common.advancements.UnlockAbilityCriterion;
import com.auroali.sanguinisluxuria.common.registry.*;
import com.auroali.sanguinisluxuria.common.rituals.AbilityRevealRitual;
import com.auroali.sanguinisluxuria.common.rituals.ItemRitual;
import com.auroali.sanguinisluxuria.common.rituals.VampireAbilityResetRitual;
import com.auroali.sanguinisluxuria.common.rituals.VampireAbilityRitual;
import com.auroali.sanguinisluxuria.datagen.builders.BloodCauldronFillRecipeJsonBuilder;
import com.auroali.sanguinisluxuria.datagen.builders.BloodCauldronRecipeJsonBuilder;
import com.auroali.sanguinisluxuria.datagen.builders.RitualRecipeJsonBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
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
        this.generateCraftingRecipes(exporter);
        this.generateFurnaceRecipes(exporter);
        this.generateSingleItemRecipes(exporter);
        this.generateCauldronInfusingRecipes(exporter);
        this.generateRitualRecipes(exporter);
    }

    public void generateFurnaceRecipes(Consumer<RecipeJsonProvider> exporter) {
        CookingRecipeJsonBuilder.createSmelting(Ingredient.ofItems(BLItems.RAW_SILVER), RecipeCategory.MISC, BLItems.SILVER_INGOT, 0.35f, 200)
          .group("smelting")
          .criterion("has_item", conditionsFromItem(BLItems.RAW_SILVER))
          .offerTo(exporter, BLResources.id("smelting/silver_ingot"));
        CookingRecipeJsonBuilder.createBlasting(Ingredient.ofItems(BLItems.RAW_SILVER), RecipeCategory.MISC, BLItems.SILVER_INGOT, 0.35f, 100)
          .group("blasting")
          .criterion("has_item", conditionsFromItem(BLItems.RAW_SILVER))
          .offerTo(exporter, BLResources.id("blasting/silver_ingot"));
    }

    public void generateSingleItemRecipes(Consumer<RecipeJsonProvider> exporter) {
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(BLTags.Items.DECAYED_LOGS), RecipeCategory.TOOLS, BLItems.MASK_1)
          .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
          .offerTo(exporter);
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(BLTags.Items.DECAYED_LOGS), RecipeCategory.TOOLS, BLItems.MASK_2)
          .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
          .offerTo(exporter);
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(BLTags.Items.DECAYED_LOGS), RecipeCategory.TOOLS, BLItems.MASK_3)
          .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
          .offerTo(exporter);
    }

    public void generateCraftingRecipes(Consumer<RecipeJsonProvider> exporter) {
        generateFamily(exporter, BLBlockFamilies.DECAYED_WOOD_FAMILY);
        offerPlanksRecipe(exporter, BLBlocks.DECAYED_PLANKS, BLTags.Items.DECAYED_LOGS, 4);
        offerHangingSignRecipe(exporter, BLBlocks.DECAYED_HANGING_SIGN, BLBlocks.STRIPPED_DECAYED_LOG);
        offerBarkBlockRecipe(exporter, BLBlocks.DECAYED_WOOD, BLBlocks.DECAYED_LOG);
        offerBarkBlockRecipe(exporter, BLBlocks.STRIPPED_DECAYED_WOOD, BLBlocks.STRIPPED_DECAYED_LOG);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, BLBlocks.GRAFTED_SAPLING)
          .input(BLItems.BLOOD_PETAL)
          .input(ItemTags.SAPLINGS)
          .criterion(hasItem(BLItems.BLOOD_PETAL), conditionsFromItem(BLItems.BLOOD_PETAL))
          .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BLBlocks.SILVER_PRESSURE_PLATE)
          .pattern("##")
          .input('#', BLTags.Items.SILVER_INGOTS)
          .criterion("has_item", conditionsFromTag(BLTags.Items.SILVER_INGOTS))
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
    }

    public void generateCauldronInfusingRecipes(Consumer<RecipeJsonProvider> exporter) {
        BloodCauldronRecipeJsonBuilder.create(RecipeCategory.BREWING, Ingredient.fromTag(ItemTags.FLOWERS), BLItems.BLOOD_PETAL)
          .criterion("become_vampire", BecomeVampireCriterion.Conditions.create())
          .offerTo(exporter);
        BloodCauldronFillRecipeJsonBuilder.create(RecipeCategory.BREWING, Ingredient.ofItems(Items.GLASS_BOTTLE), BLItems.BLOOD_BOTTLE)
          .criterion("has_item", conditionsFromItem(BLItems.BLOOD_BOTTLE))
          .offerTo(exporter);
        BloodCauldronFillRecipeJsonBuilder.create(RecipeCategory.BREWING, BLItems.BLOOD_BAG)
          .criterion("has_item", conditionsFromItem(BLItems.BLOOD_BAG))
          .offerTo(exporter);
    }

    public void generateRitualRecipes(Consumer<RecipeJsonProvider> exporter) {
        RitualRecipeJsonBuilder.create(RecipeCategory.BREWING, new ItemRitual(BLItems.TWISTED_BLOOD))
          .catalyst(BLItems.BLOOD_BOTTLE)
          .input(Items.NETHER_WART)
          .input(Items.FERMENTED_SPIDER_EYE)
          .input(BLItems.BLOOD_PETAL)
          .criterion("is_vampire", BecomeVampireCriterion.Conditions.create())
          .offerTo(exporter);
        RitualRecipeJsonBuilder.create(RecipeCategory.TOOLS, new ItemRitual(BLItems.BLOOD_BAG))
          .catalyst(Items.GLASS_BOTTLE)
          .input(Items.GLASS)
          .input(Items.GLASS)
          .criterion("has_twisted_blood", conditionsFromItem(BLItems.TWISTED_BLOOD))
          .offerTo(exporter);
        RitualRecipeJsonBuilder.create(RecipeCategory.TOOLS, new ItemRitual(BLItems.PENDANT_OF_PIERCING))
          .catalyst(Items.ARROW)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.GOLD_INGOT)
          .input(Items.STRING)
          .criterion("unlock_abilities", UnlockAbilityCriterion.Conditions.create(BLVampireAbilities.TELEPORT))
          .offerTo(exporter);

        RitualRecipeJsonBuilder.create(RecipeCategory.MISC, new VampireAbilityRitual(BLVampireAbilities.TELEPORT))
          .catalyst(Items.ENDER_PEARL)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.CHORUS_FRUIT)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.CHORUS_FRUIT)
          .criterion("has_twisted_blood", conditionsFromItem(BLItems.TWISTED_BLOOD))
          .offerTo(exporter, BLResources.id("rituals/blink"));
        RitualRecipeJsonBuilder.create(RecipeCategory.MISC, new VampireAbilityRitual(BLVampireAbilities.BITE))
          .catalyst(Items.POINTED_DRIPSTONE)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.BONE)
          .input(Items.SKELETON_SKULL)
          .input(Items.BONE)
          .criterion("has_twisted_blood", conditionsFromItem(BLItems.TWISTED_BLOOD))
          .offerTo(exporter, BLResources.id("rituals/bite"));
        RitualRecipeJsonBuilder.create(RecipeCategory.MISC, new VampireAbilityRitual(BLVampireAbilities.INFECTIOUS))
          .catalyst(Items.GLASS_BOTTLE)
          .input(BLItems.TWISTED_BLOOD)
          .input(Items.GUNPOWDER)
          .input(Items.DRAGON_BREATH)
          .input(Items.GLOWSTONE_DUST)
          .criterion("has_twisted_blood", conditionsFromItem(BLItems.TWISTED_BLOOD))
          .offerTo(exporter, BLResources.id("rituals/infectious"));

        RitualRecipeJsonBuilder.create(RecipeCategory.MISC, VampireAbilityResetRitual.INSTANCE)
          .catalyst(PotionUtil.setPotion(new ItemStack(Items.POTION), BLStatusEffects.BLESSED_WATER_POTION))
          .input(Items.SUNFLOWER)
          .input(Items.SUNFLOWER)
          .criterion("unlocked_ability", UnlockAbilityCriterion.Conditions.create())
          .offerTo(exporter, BLResources.id("rituals/reset_abilities"));

        RitualRecipeJsonBuilder.create(RecipeCategory.MISC, AbilityRevealRitual.INSTANCE)
          .catalyst(Items.WRITABLE_BOOK)
          .criterion("unlocked_ability", UnlockAbilityCriterion.Conditions.create())
          .offerTo(exporter, BLResources.id("rituals/reveal_abilities"));
    }
}
