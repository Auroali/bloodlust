package com.auroali.sanguinisluxuria.common.recipes;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeSerializers;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeTypes;
import com.auroali.sanguinisluxuria.common.rituals.Ritual;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class AltarRitualRecipe implements Recipe<Inventory> {
    protected final Identifier id;
    protected final Ritual ritual;
    protected final Ingredient catalyst;
    protected final DefaultedList<Ingredient> inputs;

    public AltarRitualRecipe(Identifier id, Ritual ritual, Ingredient catalyst, DefaultedList<Ingredient> inputs) {
        this.id = id;
        this.ritual = ritual;
        this.catalyst = catalyst;
        this.inputs = inputs;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return this.inputs;
    }

    public Ingredient getCatalyst() {
        return this.catalyst;
    }

    public Ritual getRitual() {
        return ritual;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return false;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BLRecipeSerializers.ALTAR_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return BLRecipeTypes.ALTAR_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AltarRitualRecipe> {

        @Override
        public AltarRitualRecipe read(Identifier id, JsonObject json) {
            if (!json.has("ritual"))
                throw new JsonParseException("Missing the ritual field!");
            JsonObject ritualData = json.get("ritual").getAsJsonObject();
            Ritual ritual = Ritual.RITUAL_CODEC
              .parse(JsonOps.INSTANCE, ritualData)
              .resultOrPartial(Bloodlust.LOGGER::error)
              .orElseThrow();

            Ingredient catalyst = Ingredient.fromJson(json.get("catalyst"));
            DefaultedList<Ingredient> inputs = getIngredients(json.getAsJsonArray("inputs"));

            return new AltarRitualRecipe(id, ritual, catalyst, inputs);
        }

        private static DefaultedList<Ingredient> getIngredients(JsonArray json) {
            DefaultedList<Ingredient> defaultedList = DefaultedList.of();

            for (int i = 0; i < json.size(); i++) {
                Ingredient ingredient = Ingredient.fromJson(json.get(i), false);
                if (!ingredient.isEmpty()) {
                    defaultedList.add(ingredient);
                }
            }

            return defaultedList;
        }

        @Override
        public AltarRitualRecipe read(Identifier id, PacketByteBuf buf) {
            Ritual ritual = buf.decodeAsJson(Ritual.RITUAL_CODEC);

            Ingredient catalyst = Ingredient.fromPacket(buf);
            DefaultedList<Ingredient> inputs = buf.readCollection(size -> DefaultedList.ofSize(size, Ingredient.EMPTY), Ingredient::fromPacket);
            return new AltarRitualRecipe(id, ritual, catalyst, inputs);
        }

        @Override
        public void write(PacketByteBuf buf, AltarRitualRecipe recipe) {
            buf.encodeAsJson(
              Ritual.RITUAL_CODEC,
              recipe.ritual
            );
            recipe.catalyst.write(buf);
            buf.writeCollection(recipe.inputs, (buffer, ingredient) -> ingredient.write(buffer));
        }
    }
}
