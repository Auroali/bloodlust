package com.auroali.sanguinisluxuria.compat.emi;

import com.auroali.sanguinisluxuria.common.blocks.AltarBlock;
import com.auroali.sanguinisluxuria.common.recipes.AltarRitualRecipe;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.rituals.ItemRitual;
import com.auroali.sanguinisluxuria.common.rituals.Ritual;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class AltarEmiRecipe implements EmiRecipe {
    final AltarRitualRecipe recipe;
    final EmiIngredient catalyst;
    final Ritual ritual;
    final List<EmiIngredient> inputs;
    final EmiStack output;

    public AltarEmiRecipe(AltarRitualRecipe recipe, MinecraftClient client) {
        this.recipe = recipe;
        this.ritual = recipe.getRitual();
        this.catalyst = EmiIngredient.of(recipe.getCatalyst());
        DefaultedList<EmiIngredient> stacks = DefaultedList.ofSize(8, EmiStack.EMPTY);
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            stacks.set(i, EmiIngredient.of(recipe.getIngredients().get(i)));
        }
        this.inputs = stacks;
        this.output = this.ritual instanceof ItemRitual itemRitual
          ? EmiStack.of(itemRitual.getOutputItem())
          : EmiStack.EMPTY;
    }

//    // https://github.com/emilyploszaj/emi/blob/2ac200302c2e7d551c5e7076ae03f32e4b26933b/xplat/src/main/java/dev/emi/emi/recipe/EmiShapedRecipe.java
//    public static void setRemainders(List<EmiIngredient> input, AltarRecipe recipe) {
//        try {
//            AltarInventory inv = new AltarInventory(input.size());
//            for (int i = 0; i < input.size(); i++) {
//                if (input.get(i).isEmpty()) {
//                    continue;
//                }
//                for (int j = 0; j < input.size(); j++) {
//                    if (j == i) {
//                        continue;
//                    }
//                    if (!input.get(j).isEmpty()) {
//                        inv.setStack(j, input.get(j).getEmiStacks().get(0).getItemStack().copy());
//                    }
//                }
//                List<EmiStack> stacks = input.get(i).getEmiStacks();
//                for (EmiStack stack : stacks) {
//                    inv.setStack(i, stack.getItemStack().copy());
//                    ItemStack remainder = recipe.getRemainder(inv).get(i);
//                    if (!remainder.isEmpty()) {
//                        stack.setRemainder(EmiStack.of(remainder));
//                    }
//                }
//                inv.clear();
//            }
//        } catch (Exception e) {
//            Bloodlust.LOGGER.error("Exception thrown setting remainders for " + recipe.getId(), e);
//        }
//    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiCompat.ALTAR_RECIPE_CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return this.recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(this.output);
    }

    @Override
    public int getDisplayWidth() {
        return 138;
    }

    @Override
    public int getDisplayHeight() {
        return 100;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < this.inputs.size() / 2; i++) {
            int x = 32 + (int) (32 * Math.cos((MathHelper.HALF_PI / 2 + i * 2 * MathHelper.TAU / this.inputs.size())));
            int y = 40 + (int) (32 * Math.sin((MathHelper.HALF_PI / 2 + i * 2 * MathHelper.TAU / this.inputs.size())));
            widgets.addSlot(this.inputs.get(i), x, y);
        }
        for (int i = this.inputs.size() / 2; i < this.inputs.size(); i++) {
            int x = 32 + (int) (32 * Math.cos((i * 2 * MathHelper.TAU / this.inputs.size())));
            int y = 40 + (int) (32 * Math.sin((i * 2 * MathHelper.TAU / this.inputs.size())));
            widgets.addSlot(this.inputs.get(i), x, y);
        }

        widgets.addSlot(this.catalyst, 32, 30)
          .drawBack(false);

        widgets.addDrawable(30, 62, 16, 16, (drawContext, mouseX, mouseY, delta) -> {
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
        widgets.addFillingArrow(84, 40, 15000)
          .tooltip(List.of(TooltipComponent.of(Text.translatable("emi.cooking.time", 15).asOrderedText())));
        widgets.addSlot(this.output, 111, 36)
          .large(true)
          .backgroundTexture(EmiCompat.TEXTURES, 0, 32)
          .recipeContext(this);
    }
}
