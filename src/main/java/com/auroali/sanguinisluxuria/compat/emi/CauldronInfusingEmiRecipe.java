package com.auroali.sanguinisluxuria.compat.emi;

import com.auroali.sanguinisluxuria.common.recipes.BloodCauldronRecipe;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class CauldronInfusingEmiRecipe implements EmiRecipe {
    final BloodCauldronRecipe recipe;
    final List<EmiIngredient> inputs;
    final List<EmiStack> outputs;

    public CauldronInfusingEmiRecipe(BloodCauldronRecipe recipe, MinecraftClient client) {
        this.recipe = recipe;
        this.inputs = List.of(EmiIngredient.of(recipe.getIngredients().get(0)));
        this.outputs = List.of(EmiStack.of(recipe.getOutput(client.world.getRegistryManager())));
        ;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiCompat.BLOOD_CAULDRON_RECIPE_CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 64;
    }

    @Override
    public int getDisplayHeight() {
        return 64;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 3, 6);
        widgets.addDrawable(4, 36, 16, 16, (drawContext, mouseX, mouseY, delta) -> {
            MatrixStack stack = drawContext.getMatrices();
            MinecraftClient client = MinecraftClient.getInstance();
            BlockState state = BLBlocks.BLOOD_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, recipe.getCauldronLevel());
            BlockRenderManager blockRenderer = client.getBlockRenderManager();
            stack.push();
            stack.translate(-3, 16, 140);
            stack.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
            stack.scale(16, 16, 16);
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
            blockRenderer.renderBlockAsEntity(state, stack, drawContext.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            stack.pop();
        }).tooltip(List.of(TooltipComponent.of(Text.translatable("gui.sanguinisluxuria.blood_bottle_tooltip", recipe.getCauldronLevel()).asOrderedText())));
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 24, 36);
        widgets.addSlot(outputs.get(0), 47, 36).drawBack(false).recipeContext(this);
    }
}
