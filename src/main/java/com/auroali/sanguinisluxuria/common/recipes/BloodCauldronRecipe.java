package com.auroali.sanguinisluxuria.common.recipes;

import com.auroali.sanguinisluxuria.common.registry.BLRecipeSerializers;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeTypes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BloodCauldronRecipe implements Recipe<SimpleInventory> {
    protected final Ingredient ingredient;
    protected final int level;
    protected final Identifier id;
    protected final ItemStack result;

    public BloodCauldronRecipe(Identifier id, int level, Ingredient ingredient, ItemStack result) {
        this.id = id;
        this.level = level;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return inventory.size() == 1 && this.ingredient.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        return this.result.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width == 1 && height == 1;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.result;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, this.ingredient);
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BLRecipeSerializers.BLOOD_CAULDRON_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return BLRecipeTypes.BLOOD_CAULDRON_TYPE;
    }

    public int getCauldronLevel() {
        return this.level;
    }

    public static class Serializer implements RecipeSerializer<BloodCauldronRecipe> {
        @Override
        public BloodCauldronRecipe read(Identifier id, JsonObject json) {
            if (!json.has("input"))
                throw new JsonParseException("Missing recipe input!");
            if (!json.has("result"))
                throw new JsonParseException("Missing recipe result!");
            int level = 1;
            if (json.has("level"))
                level = json.get("level").getAsInt();
            Ingredient ingredient = Ingredient.fromJson(json.get("input"));
            ItemStack result = ShapedRecipe.outputFromJson(json.get("result").getAsJsonObject());
            return new BloodCauldronRecipe(id, level, ingredient, result);
        }

        @Override
        public BloodCauldronRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient ingredient = Ingredient.fromPacket(buf);
            int level = buf.readVarInt();
            ItemStack result = buf.readItemStack();
            return new BloodCauldronRecipe(id, level, ingredient, result);
        }

        @Override
        public void write(PacketByteBuf buf, BloodCauldronRecipe recipe) {
            recipe.ingredient.write(buf);
            buf.writeVarInt(recipe.getCauldronLevel());
            buf.writeItemStack(recipe.result);
        }
    }
}
