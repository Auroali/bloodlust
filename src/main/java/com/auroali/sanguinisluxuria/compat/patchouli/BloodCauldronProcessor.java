package com.auroali.sanguinisluxuria.compat.patchouli;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.recipes.BloodCauldronRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.Optional;

public class BloodCauldronProcessor implements IComponentProcessor {
    private BloodCauldronRecipe recipe;

    @Override
    public void setup(World world, IVariableProvider variables) {
        String id = variables.get("recipe").asString();
        RecipeManager manager = world.getRecipeManager();
        this.recipe = manager.get(new Identifier(id))
          .flatMap(r -> r instanceof BloodCauldronRecipe bloodCauldronRecipe ? Optional.of(bloodCauldronRecipe) : Optional.empty())
          .orElse(null);
        if (this.recipe == null) {
            Bloodlust.LOGGER.warn("Could not find blood cauldron recipe {} for patchouli entry", id);
        }
    }

    @Override
    public IVariable process(World world, String key) {
        if (this.recipe == null)
            return null;

        if (key.startsWith("item")) {
            int i = Integer.parseInt(key.substring(4)) - 1;
            if (i < this.recipe.getIngredients().size()) {
                Ingredient ingredient = this.recipe.getIngredients().get(i);
                return IVariable.from(ingredient.getMatchingStacks());
            }
            return IVariable.from(ItemStack.EMPTY);
        }
        if (key.equals("output"))
            return IVariable.from(this.recipe.getOutput(world.getRegistryManager()));
        return null;
    }
}
