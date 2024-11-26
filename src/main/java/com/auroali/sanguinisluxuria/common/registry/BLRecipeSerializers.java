package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.recipes.AltarRitualRecipe;
import com.auroali.sanguinisluxuria.common.recipes.BloodCauldronFillRecipe;
import com.auroali.sanguinisluxuria.common.recipes.BloodCauldronRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BLRecipeSerializers {
    public static final RecipeSerializer<AltarRitualRecipe> ALTAR_RECIPE_SERIALIZER = new AltarRitualRecipe.Serializer();
    public static final RecipeSerializer<BloodCauldronRecipe> BLOOD_CAULDRON_SERIALIZER = new BloodCauldronRecipe.Serializer();
    public static final RecipeSerializer<BloodCauldronRecipe> BLOOD_CAULDRON_FILL_SERIALIZER = new BloodCauldronFillRecipe.Serializer();

    public static void register() {
        Registry.register(Registries.RECIPE_SERIALIZER, BLResources.ALTAR_RECIPE_ID, ALTAR_RECIPE_SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, BLResources.BLOOD_CAULDRON_ID, BLOOD_CAULDRON_SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, BLResources.BLOOD_CAULDRON_FILL_ID, BLOOD_CAULDRON_FILL_SERIALIZER);
    }
}
