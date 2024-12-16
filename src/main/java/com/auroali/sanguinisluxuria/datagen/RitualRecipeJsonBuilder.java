package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeSerializers;
import com.auroali.sanguinisluxuria.common.rituals.ItemRitual;
import com.auroali.sanguinisluxuria.common.rituals.Ritual;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RitualRecipeJsonBuilder extends RecipeJsonBuilder {
    private final Ritual ritual;
    private String group;
    private Ingredient catalyst;
    private final List<Ingredient> inputs = new ArrayList<>();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
    private final RecipeCategory category;

    RitualRecipeJsonBuilder(RecipeCategory category, Ritual ritual) {
        this.ritual = ritual;
        this.category = category;
    }

    public static RitualRecipeJsonBuilder create(RecipeCategory category, Ritual ritual) {
        return new RitualRecipeJsonBuilder(category, ritual);
    }

    public RitualRecipeJsonBuilder catalyst(ItemConvertible item) {
        return this.catalyst(Ingredient.ofItems(item));
    }

    public RitualRecipeJsonBuilder catalyst(TagKey<Item> tag) {
        return this.catalyst(Ingredient.fromTag(tag));
    }

    public RitualRecipeJsonBuilder catalyst(Ingredient ingredient) {
        this.catalyst = ingredient;
        return this;
    }

    public RitualRecipeJsonBuilder catalyst(ItemStack stack) {
        return this.catalyst(stack, true);
    }

    public RitualRecipeJsonBuilder catalyst(ItemStack stack, boolean nbt) {
        if (nbt)
            return this.catalyst(DefaultCustomIngredients.nbt(stack, false));
        return this.catalyst(Ingredient.ofStacks(stack));
    }

    public RitualRecipeJsonBuilder input(ItemConvertible item) {
        return this.input(Ingredient.ofItems(item));
    }

    public RitualRecipeJsonBuilder input(ItemStack item) {
        return this.input(item, true);
    }

    public RitualRecipeJsonBuilder input(ItemStack item, boolean nbtIngredient) {
        if (nbtIngredient && item.getNbt() != null)
            return this.input(DefaultCustomIngredients.nbt(item, false));
        return this.input(Ingredient.ofStacks(item));
    }


    public RitualRecipeJsonBuilder input(TagKey<Item> item) {
        return this.input(Ingredient.fromTag(item));
    }

    public RitualRecipeJsonBuilder input(Ingredient ingredient) {
        this.inputs.add(ingredient);
        return this;
    }

    public RitualRecipeJsonBuilder criterion(String name, CriterionConditions conditions) {
        this.advancementBuilder.criterion(name, conditions);
        return this;
    }

    public RitualRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public void validate(Identifier id) {
        if (this.catalyst.isEmpty())
            throw new IllegalStateException("Recipe " + id + " must have a catalyst");
        if (this.inputs.size() > 8)
            throw new IllegalStateException("Recipe " + id + " must have at most 8 inputs");
        if (this.advancementBuilder.getCriteria().isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
    }

    protected Identifier getId() {
        if (this.ritual instanceof ItemRitual itemRitual) {
            return CraftingRecipeJsonBuilder.getItemId(itemRitual.stack().getItem());
        }
        throw new IllegalArgumentException("Non-item ritual recipes must manually specify an id");
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        this.offerTo(exporter, this.getId());
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        this.validate(recipeId);
        this.advancementBuilder
          .parent(CraftingRecipeJsonBuilder.ROOT)
          .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
          .rewards(AdvancementRewards.Builder.recipe(recipeId))
          .criteriaMerger(CriterionMerger.OR);
        exporter.accept(
          new Provider(
            this.advancementBuilder,
            recipeId,
            recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/"),
            getCraftingCategory(this.category),
            this.group,
            this.ritual,
            this.catalyst,
            this.inputs
          )
        );
    }

    public static class Provider implements RecipeJsonProvider {
        final String group;
        private final Ritual ritual;
        private final Ingredient catalyst;
        public final List<Ingredient> inputs;
        private final Advancement.Builder advancementBuilder;
        private final Identifier id;
        private final Identifier advancementId;
        private final CraftingRecipeCategory category;

        public Provider(Advancement.Builder advancementBuilder, Identifier recipeId, Identifier advancementId, CraftingRecipeCategory category, String group, Ritual ritual, Ingredient catalyst, List<Ingredient> inputs) {
            this.advancementBuilder = advancementBuilder;
            this.group = group == null ? "" : group;
            this.ritual = ritual;
            this.catalyst = catalyst;
            this.inputs = inputs;
            this.id = recipeId;
            this.advancementId = advancementId;
            this.category = category;
        }

        @Override
        public void serialize(JsonObject json) {
            json.addProperty("category", this.category.asString());
            JsonArray ingredientsJson = new JsonArray();
            for (Ingredient i : this.inputs) {
                ingredientsJson.add(i.toJson());
            }
            json.add("inputs", ingredientsJson);
            json.add("catalyst", this.catalyst.toJson());

            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonElement ritualData = Ritual.RITUAL_CODEC
              .encodeStart(JsonOps.INSTANCE, this.ritual)
              .resultOrPartial(Bloodlust.LOGGER::error)
              .orElseThrow();
            json.add("ritual", ritualData);
        }

        @Override
        public Identifier getRecipeId() {
            return this.id;
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
            return this.advancementId;
        }
    }
}
