package com.auroali.sanguinisluxuria.compat.patchouli;

import com.auroali.sanguinisluxuria.common.recipes.AltarRitualRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.text.Text;
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
        recipe = (AltarRitualRecipe) manager.get(new Identifier(id)).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public IVariable process(World world, String key) {
        if (key.startsWith("item")) {
            int i = Integer.parseInt(key.substring(4)) - 1;
            if (i < recipe.getIngredients().size()) {
                Ingredient ingredient = recipe.getIngredients().get(i);
                return IVariable.from(ingredient.getMatchingStacks());
            }
            return IVariable.from(ItemStack.EMPTY);
        }
        if (key.equals("output"))
            return IVariable.from(recipe.getOutput(world.getRegistryManager()));
        if (key.equals("time")) {
            return IVariable.from(Text.of("%ds".formatted(15)));
        }
        return null;
    }
}
