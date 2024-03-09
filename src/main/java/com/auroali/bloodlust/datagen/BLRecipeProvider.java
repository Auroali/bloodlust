package com.auroali.bloodlust.datagen;

import com.auroali.bloodlust.common.advancements.BecomeVampireCriterion;
import com.auroali.bloodlust.common.registry.BLBlocks;
import com.auroali.bloodlust.common.registry.BLItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SingleItemRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ItemTags;

import java.util.function.Consumer;

public class BLRecipeProvider extends FabricRecipeProvider {
    public BLRecipeProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(ItemTags.LOGS), BLItems.MASK_1)
                .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
                .offerTo(exporter);
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(ItemTags.LOGS), BLItems.MASK_2)
                .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
                .offerTo(exporter);
        SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.fromTag(ItemTags.LOGS), BLItems.MASK_3)
                .criterion("has_log", conditionsFromTag(ItemTags.LOGS))
                .offerTo(exporter);
        AltarRecipeJsonBuilder.create(BLItems.TWISTED_BLOOD)
                .input(BLItems.BLOOD_BOTTLE)
                .input(Items.NETHER_WART)
                .input(Items.WITHER_ROSE)
                .input(Items.TWISTING_VINES)
                .criterion("is_vampire", BecomeVampireCriterion.Conditions.create())
                .offerTo(exporter);
        AltarRecipeJsonBuilder.create(BLItems.BLOOD_BAG)
                .input(BLItems.TWISTED_BLOOD)
                .input(Items.GLASS_BOTTLE)
                .input(Items.GLASS)
                .input(Items.GLASS)
                .criterion("has_twisted_blood", conditionsFromItem(BLItems.TWISTED_BLOOD))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(BLBlocks.SKILL_UPGRADER)
                .pattern("lbl")
                .pattern("sss")
                .input('b', BLItems.BLOOD_BOTTLE)
                .input('s', Items.BLACKSTONE)
                .input('l', ItemTags.LOGS)
                .criterion("is_vampire", BecomeVampireCriterion.Conditions.create())
                .criterion("has_blackstone", conditionsFromItem(Items.BLACKSTONE))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(BLBlocks.PEDESTAL)
                .pattern(" l ")
                .pattern(" s ")
                .input('s', Items.BLACKSTONE_WALL)
                .input('l', ItemTags.LOGS)
                .criterion("has_blackstone", conditionsFromItem(Items.BLACKSTONE))
                .offerTo(exporter);
        AltarRecipeJsonBuilder.create(BLItems.BLESSED_BLOOD)
                .input(BLItems.TWISTED_BLOOD)
                // minecraft doesn't support nbt in ingredients so i cant use blessed water ;-;
                .input(Items.ENCHANTED_GOLDEN_APPLE)
                .input(Items.SUNFLOWER)
                .input(Items.HONEY_BOTTLE)
                .criterion("drink_twisted_blood", ConsumeItemCriterion.Conditions.item(BLItems.TWISTED_BLOOD))
                .offerTo(exporter);
    }
}
