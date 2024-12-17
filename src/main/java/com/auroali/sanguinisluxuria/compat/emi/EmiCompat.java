package com.auroali.sanguinisluxuria.compat.emi;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.recipes.AltarRitualRecipe;
import com.auroali.sanguinisluxuria.common.recipes.BloodCauldronRecipe;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeTypes;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

public class EmiCompat implements EmiPlugin {
    public static final Identifier TEXTURES = BLResources.id("textures/gui/emi.png");
    public static final EmiStack ALTAR = EmiStack.of(BLBlocks.ALTAR);
    public static final EmiStack PEDESTAL = EmiStack.of(BLBlocks.PEDESTAL);
    public static final EmiStack CAULDRON = EmiStack.of(Items.CAULDRON);
    public static final EmiRecipeCategory ALTAR_RECIPE_CATEGORY = new EmiRecipeCategory(BLResources.ALTAR_ID, ALTAR, new EmiTexture(TEXTURES, 0, 16, 16, 16), EmiRecipeSorting.compareOutputThenInput());
    public static final EmiRecipeCategory BLOOD_CAULDRON_RECIPE_CATEGORY = new EmiRecipeCategory(BLResources.BLOOD_CAULDRON_ID, CAULDRON, new EmiTexture(BLResources.id("textures/gui/emi.png"), 0, 0, 16, 16), EmiRecipeSorting.compareOutputThenInput());

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ALTAR_RECIPE_CATEGORY);
        registry.addCategory(BLOOD_CAULDRON_RECIPE_CATEGORY);
        registry.addWorkstation(ALTAR_RECIPE_CATEGORY, ALTAR);
        registry.addWorkstation(ALTAR_RECIPE_CATEGORY, PEDESTAL);
        registry.addWorkstation(BLOOD_CAULDRON_RECIPE_CATEGORY, CAULDRON);

        RecipeManager manager = registry.getRecipeManager();
        for (AltarRitualRecipe recipe : manager.listAllOfType(BLRecipeTypes.ALTAR_RECIPE)) {
            registry.addRecipe(new AltarEmiRecipe(recipe));
        }
        for (BloodCauldronRecipe recipe : manager.listAllOfType(BLRecipeTypes.BLOOD_CAULDRON_TYPE)) {
            registry.addRecipe(new CauldronInfusingEmiRecipe(recipe, MinecraftClient.getInstance()));
        }
    }
}
