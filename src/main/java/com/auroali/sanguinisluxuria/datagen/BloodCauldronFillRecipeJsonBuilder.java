package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLRecipeSerializers;
import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BloodCauldronFillRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final Item output;
    private final int outputCount;
    private String group;
    private final Ingredient ingredient;
    private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
    private final RecipeCategory category;

    BloodCauldronFillRecipeJsonBuilder(RecipeCategory category, Ingredient ingredient, Item output, int outputCount) {
        this.output = output;
        this.outputCount = outputCount;
        this.ingredient = ingredient;
        this.category = category;
    }

    public static BloodCauldronFillRecipeJsonBuilder create(RecipeCategory category, Ingredient ingredient, ItemConvertible output) {
        return new BloodCauldronFillRecipeJsonBuilder(category, ingredient, output.asItem(), 1);
    }

    public static BloodCauldronFillRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return new BloodCauldronFillRecipeJsonBuilder(category, Ingredient.ofItems(output), output.asItem(), 1);
    }

    @Override
    public BloodCauldronFillRecipeJsonBuilder criterion(String name, CriterionConditions conditions) {
        this.advancementBuilder.criterion(name, conditions);
        return this;
    }

    @Override
    public BloodCauldronFillRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output;
    }

    public void validate(Identifier id) {
        if (this.advancementBuilder.getCriteria().isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        Identifier id = CraftingRecipeJsonBuilder.getItemId(this.getOutputItem());
        this.offerTo(exporter, new Identifier(id.getNamespace(), "cauldron_filling/" + id.getPath()));
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        this.validate(recipeId);
        this.advancementBuilder
          .parent(ROOT)
          .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
          .rewards(AdvancementRewards.Builder.recipe(recipeId))
          .criteriaMerger(CriterionMerger.OR);
        exporter.accept(
          new Provider(
            recipeId,
            recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/"),
            this,
            getCraftingCategory(this.category)
          )
        );
    }

    public static class Provider implements RecipeJsonProvider {
        final Item output;
        final int outputCount;
        final String group;
        public final Ingredient ingredient;
        private final Advancement.Builder advancementBuilder;
        private final Identifier id;
        private final Identifier advancementId;
        private final CraftingRecipeCategory category;

        public Provider(Identifier recipeId, Identifier advancementId, BloodCauldronFillRecipeJsonBuilder builder, CraftingRecipeCategory category) {
            this.outputCount = builder.outputCount;
            this.output = builder.output;
            this.advancementBuilder = builder.advancementBuilder;
            this.group = builder.group == null ? "" : builder.group;
            this.ingredient = builder.ingredient;
            this.id = recipeId;
            this.advancementId = advancementId;
            this.category = category;
        }

        @Override
        public void serialize(JsonObject json) {
            json.addProperty("category", this.category.asString());
            json.add("input", this.ingredient.toJson());

            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonObject result = new JsonObject();
            result.addProperty("item", Registries.ITEM.getId(this.output).toString());
            if (this.outputCount > 1) {
                result.addProperty("count", this.outputCount);
            }
            json.add("result", result);
        }

        @Override
        public Identifier getRecipeId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return BLRecipeSerializers.BLOOD_CAULDRON_FILL_SERIALIZER;
        }

        @Nullable
        @Override
        public JsonObject toAdvancementJson() {
            return this.advancementBuilder.toJson();
        }

        @Nullable
        @Override
        public Identifier getAdvancementId() {
            return this.advancementId;
        }
    }
}
