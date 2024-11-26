package com.auroali.sanguinisluxuria.compat.emi;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.blocks.AltarBlock;
import com.auroali.sanguinisluxuria.common.recipes.AltarInventory;
import com.auroali.sanguinisluxuria.common.recipes.AltarRecipe;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class AltarEmiRecipe implements EmiRecipe {
    final AltarRecipe recipe;
    final EmiIngredient catalyst;
    final List<EmiIngredient> inputs;
    final List<EmiStack> outputs;

    public AltarEmiRecipe(AltarRecipe recipe, MinecraftClient client) {
        this.recipe = recipe;
        this.catalyst = EmiIngredient.of(recipe.getCatalyst());
        DefaultedList<EmiIngredient> stacks = DefaultedList.ofSize(8, EmiStack.EMPTY);
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            stacks.set(i, EmiIngredient.of(recipe.getIngredients().get(i)));
        }
        this.inputs = stacks;
        this.outputs = List.of(EmiStack.of(recipe.getOutput(client.world.getRegistryManager())));
        setRemainders(stacks, recipe);
    }

    // https://github.com/emilyploszaj/emi/blob/2ac200302c2e7d551c5e7076ae03f32e4b26933b/xplat/src/main/java/dev/emi/emi/recipe/EmiShapedRecipe.java
    public static void setRemainders(List<EmiIngredient> input, AltarRecipe recipe) {
        try {
            AltarInventory inv = new AltarInventory(input.size());
            for (int i = 0; i < input.size(); i++) {
                if (input.get(i).isEmpty()) {
                    continue;
                }
                for (int j = 0; j < input.size(); j++) {
                    if (j == i) {
                        continue;
                    }
                    if (!input.get(j).isEmpty()) {
                        inv.setStack(j, input.get(j).getEmiStacks().get(0).getItemStack().copy());
                    }
                }
                List<EmiStack> stacks = input.get(i).getEmiStacks();
                for (EmiStack stack : stacks) {
                    inv.setStack(i, stack.getItemStack().copy());
                    ItemStack remainder = recipe.getRemainder(inv).get(i);
                    if (!remainder.isEmpty()) {
                        stack.setRemainder(EmiStack.of(remainder));
                    }
                }
                inv.clear();
            }
        } catch (Exception e) {
            Bloodlust.LOGGER.error("Exception thrown setting remainders for " + recipe.getId(), e);
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiCompat.ALTAR_RECIPE_CATEGORY;
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
        return 128;
    }

    @Override
    public int getDisplayHeight() {
        return 100;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < inputs.size() / 2; i++) {
            int x = 32 + (int) (32 * Math.cos((MathHelper.HALF_PI / 2 + i * 2 * MathHelper.TAU / inputs.size())));
            int y = 40 + (int) (32 * Math.sin((MathHelper.HALF_PI / 2 + i * 2 * MathHelper.TAU / inputs.size())));
            widgets.addSlot(inputs.get(i), x, y);
        }
        for (int i = inputs.size() / 2; i < inputs.size(); i++) {
            int x = 32 + (int) (32 * Math.cos((i * 2 * MathHelper.TAU / inputs.size())));
            int y = 40 + (int) (32 * Math.sin((i * 2 * MathHelper.TAU / inputs.size())));
            widgets.addSlot(inputs.get(i), x, y);
        }

        widgets.addSlot(this.catalyst, 32, 30)
          .drawBack(false);

        widgets.addDrawable(30, 58, 16, 16, (drawContext, mouseX, mouseY, delta) -> {
            MatrixStack stack = drawContext.getMatrices();
            MinecraftClient client = MinecraftClient.getInstance();
            BlockState state = BLBlocks.ALTAR.getDefaultState().with(AltarBlock.ACTIVE, true);
            BlockRenderManager blockRenderer = client.getBlockRenderManager();
            stack.push();
            stack.translate(0, 0, 140);
            stack.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
            stack.scale(16, 16, 16);
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
            blockRenderer.renderBlockAsEntity(state, stack, drawContext.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            stack.pop();
        });
        widgets.addFillingArrow(84, 40, recipe.getProcessingTicks() * 50)
          .tooltip(List.of(TooltipComponent.of(Text.translatable("emi.cooking.time", recipe.getProcessingTicks() / 20f).asOrderedText())));
        widgets.addSlot(outputs.get(0), 109, 40).recipeContext(this);
    }
}
