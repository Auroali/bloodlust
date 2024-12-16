package com.auroali.sanguinisluxuria.compat.patchouli;

import com.auroali.sanguinisluxuria.common.recipes.AltarRitualRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class AltarRecipeProcessor implements IComponentProcessor {
    private AltarRitualRecipe recipe;

    @Override
    public void setup(World world, IVariableProvider variables) {
        String id = variables.get("recipe").asString();
        RecipeManager manager = world.getRecipeManager();
        this.recipe = (AltarRitualRecipe) manager.get(new Identifier(id)).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public IVariable process(World world, String key) {
        if (key.startsWith("input")) {
            int i = Integer.parseInt(key.substring(5)) - 1;
            if (i < this.recipe.getIngredients().size()) {
                Ingredient ingredient = this.recipe.getIngredients().get(i);
                return IVariable.from(ingredient.getMatchingStacks());
            }
            return IVariable.empty();
        }
        if (key.startsWith("catalyst")) {
            return IVariable.from(this.recipe.getCatalyst().getMatchingStacks());
        }

        return null;
    }
}
