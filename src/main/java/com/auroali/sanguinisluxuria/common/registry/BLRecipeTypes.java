package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.recipes.AltarRitualRecipe;
import com.auroali.sanguinisluxuria.common.recipes.BloodCauldronRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BLRecipeTypes {
    public static final RecipeType<AltarRitualRecipe> ALTAR_RECIPE = new RecipeType<>() {
        @Override
        public String toString() {
            return BLResources.ALTAR_RECIPE_ID.toString();
        }
    };
    public static final RecipeType<BloodCauldronRecipe> BLOOD_CAULDRON_TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return BLResources.BLOOD_CAULDRON_ID.toString();
        }
    };

    public static void register() {
        Registry.register(Registries.RECIPE_TYPE, BLResources.ALTAR_RECIPE_ID, ALTAR_RECIPE);
        Registry.register(Registries.RECIPE_TYPE, BLResources.BLOOD_CAULDRON_ID, BLOOD_CAULDRON_TYPE);
    }
}
