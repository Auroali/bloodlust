package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLRecipeSerializers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
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
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AltarRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final Item output;
    private final int outputCount;
    private String group;
    private int minLevel = 0;
    private int maxLevel = Integer.MAX_VALUE;
    private int ticksToProcess = 300;
    private final Ingredient catalyst;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
    private final RecipeCategory category;

    AltarRecipeJsonBuilder(RecipeCategory category, Item output, int outputCount, Ingredient catalyst) {
        this.output = output;
        this.outputCount = outputCount;
        this.category = category;
        this.catalyst = catalyst;
    }

    public static AltarRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, Ingredient catalyst) {
        return new AltarRecipeJsonBuilder(category, output.asItem(), 1, catalyst);
    }

    public static AltarRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count, Ingredient catalyst) {
        return new AltarRecipeJsonBuilder(category, output.asItem(), count, catalyst);
    }

    public static AltarRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, Item catalyst) {
        return new AltarRecipeJsonBuilder(category, output.asItem(), 1, Ingredient.ofItems(catalyst));
    }

    public static AltarRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count, Item catalyst) {
        return new AltarRecipeJsonBuilder(category, output.asItem(), count, Ingredient.ofItems(catalyst));
    }

    public AltarRecipeJsonBuilder input(ItemConvertible item) {
        return input(Ingredient.ofItems(item));
    }

    public AltarRecipeJsonBuilder input(ItemStack item) {
        return input(item, true);
    }

    public AltarRecipeJsonBuilder input(ItemStack item, boolean nbtIngredient) {
        if (nbtIngredient && item.getNbt() != null)
            return input(DefaultCustomIngredients.nbt(item, false));
        return input(Ingredient.ofStacks(item));
    }


    public AltarRecipeJsonBuilder input(TagKey<Item> item) {
        return input(Ingredient.fromTag(item));
    }

    public AltarRecipeJsonBuilder minLevel(int minLevel) {
        this.minLevel = minLevel;
        return this;
    }

    public AltarRecipeJsonBuilder maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    public AltarRecipeJsonBuilder input(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public AltarRecipeJsonBuilder ticks(int ticksToProcess) {
        this.ticksToProcess = ticksToProcess;
        return this;
    }

    @Override
    public AltarRecipeJsonBuilder criterion(String name, CriterionConditions conditions) {
        this.advancementBuilder.criterion(name, conditions);
        return this;
    }

    @Override
    public AltarRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return output;
    }

    public void validate(Identifier id) {
        if (this.ingredients.isEmpty())
            throw new IllegalStateException("Recipe " + id + " must have at least 1 input");
        if (this.ingredients.size() > 8)
            throw new IllegalStateException("Recipe " + id + " must have at most 8 inputs");
        if (this.advancementBuilder.getCriteria().isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        validate(recipeId);
        this.advancementBuilder
          .parent(ROOT)
          .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
          .rewards(AdvancementRewards.Builder.recipe(recipeId))
          .criteriaMerger(CriterionMerger.OR);
        exporter.accept(
          new Provider(
            recipeId,
            recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/"),
            getCraftingCategory(this.category),
            this
          )
        );
    }

    public static class Provider implements RecipeJsonProvider {
        final Item output;
        final int outputCount;
        final String group;
        final int minLevel;
        final int maxLevel;
        final int ticksToProcess;
        private final Ingredient catalyst;
        public final List<Ingredient> ingredients;
        private final Advancement.Builder advancementBuilder;
        private final Identifier id;
        private final Identifier advancementId;
        private final CraftingRecipeCategory category;

        public Provider(Identifier recipeId, Identifier advancementId, CraftingRecipeCategory category, AltarRecipeJsonBuilder builder) {
            this.outputCount = builder.outputCount;
            this.output = builder.output;
            this.advancementBuilder = builder.advancementBuilder;
            this.group = builder.group == null ? "" : builder.group;
            this.catalyst = builder.catalyst;
            this.ingredients = builder.ingredients;
            this.maxLevel = builder.maxLevel;
            this.minLevel = builder.minLevel;
            this.ticksToProcess = builder.ticksToProcess;
            this.id = recipeId;
            this.advancementId = advancementId;
            this.category = category;
        }

        @Override
        public void serialize(JsonObject json) {
            json.addProperty("category", category.asString());
            JsonArray ingredientsJson = new JsonArray();
            for (Ingredient i : ingredients) {
                ingredientsJson.add(i.toJson());
            }
            json.add("input", ingredientsJson);
            json.add("catalyst", catalyst.toJson());

            json.addProperty("minLevel", minLevel);
            if (maxLevel != Integer.MAX_VALUE)
                json.addProperty("maxLevel", maxLevel);

            json.addProperty("ticksToProcess", ticksToProcess);

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
            return id;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return BLRecipeSerializers.ALTAR_RECIPE_SERIALIZER;
        }

        @Nullable
        @Override
        public JsonObject toAdvancementJson() {
            return this.advancementBuilder.toJson();
        }

        @Nullable
        @Override
        public Identifier getAdvancementId() {
            return advancementId;
        }
    }
}
