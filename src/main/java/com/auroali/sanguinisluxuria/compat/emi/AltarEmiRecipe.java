package com.auroali.sanguinisluxuria.compat.emi;

import com.auroali.sanguinisluxuria.common.blocks.AltarBlock;
import com.auroali.sanguinisluxuria.common.recipes.AltarRitualRecipe;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.auroali.sanguinisluxuria.common.rituals.ItemCreatingRitual;
import com.auroali.sanguinisluxuria.common.rituals.Ritual;
import com.google.common.collect.Lists;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
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
    final String ritualTranslationKey;

    public AltarEmiRecipe(AltarRitualRecipe recipe) {
        this.recipe = recipe;
        this.ritual = recipe.getRitual();
        this.catalyst = EmiIngredient.of(recipe.getCatalyst());
        DefaultedList<EmiIngredient> stacks = DefaultedList.ofSize(8, EmiStack.EMPTY);
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            stacks.set(i, EmiIngredient.of(recipe.getIngredients().get(i)));
        }
        this.inputs = stacks;
        this.output = this.ritual instanceof ItemCreatingRitual itemRitual
          ? EmiStack.of(itemRitual.getOutput())
          : EmiStack.EMPTY;
        this.ritualTranslationKey = Util.createTranslationKey("altar_ritual", BLRegistries.RITUAL_TYPES.getId(this.ritual.getType()));
    }

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

        // if there is no output item, create texture slot with a tooltip
        if (!this.output.isEmpty()) {
            widgets.addSlot(this.output, 111, 36)
              .large(true)
              .backgroundTexture(EmiCompat.TEXTURES, 0, 32)
              .recipeContext(this);
        }
        if (this.output.isEmpty()) {
            List<Text> tooltips = Lists.newArrayList(
              Text.translatable(this.ritualTranslationKey).formatted(Formatting.GOLD)
            );
            this.ritual.appendTooltips(tooltips);
            widgets.addTexture(EmiCompat.TEXTURES, 111, 36, 26, 26, 0, 32)
              .tooltipText(tooltips);
        }
    }
}
