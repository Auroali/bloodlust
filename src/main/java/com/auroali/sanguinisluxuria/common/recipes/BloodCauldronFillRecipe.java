package com.auroali.sanguinisluxuria.common.recipes;

import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeSerializers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class BloodCauldronFillRecipe extends BloodCauldronRecipe {
    public BloodCauldronFillRecipe(Identifier id, Ingredient ingredient, ItemStack result) {
        super(id, 1, ingredient, BloodStorageItem.setItemBlood(result, BloodStorageItem.getItemMaxBlood(result)));
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        ItemStack stack = inventory.getStack(0);
        return this.ingredient.test(inventory.getStack(0))
          && (!BloodStorageItem.isItemFillable(stack) || BloodStorageItem.getItemMaxBlood(stack) - BloodStorageItem.getItemBlood(stack) >= BloodConstants.BLOOD_PER_BOTTLE);
    }

    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack stack = inventory.getStack(0).copy();
        if (!stack.isOf(this.result.getItem())) {
            NbtCompound tag = stack.getNbt();
            stack = new ItemStack(this.result.getItem());
            stack.setNbt(tag);
        }

        BloodStorageItem.incrementItemBlood(stack, BloodConstants.BLOOD_PER_BOTTLE);
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BLRecipeSerializers.BLOOD_CAULDRON_FILL_SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<BloodCauldronRecipe> {
        @Override
        public BloodCauldronRecipe read(Identifier id, JsonObject json) {
            if (!json.has("input"))
                throw new JsonParseException("Missing recipe input!");
            if (!json.has("result"))
                throw new JsonParseException("Missing recipe result!");
            Ingredient ingredient = Ingredient.fromJson(json.get("input"));
            ItemStack result = ShapedRecipe.outputFromJson(json.get("result").getAsJsonObject());
            return new BloodCauldronFillRecipe(id, ingredient, result);
        }

        @Override
        public BloodCauldronRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient ingredient = Ingredient.fromPacket(buf);
            ItemStack result = buf.readItemStack();
            return new BloodCauldronFillRecipe(id, ingredient, result);
        }

        @Override
        public void write(PacketByteBuf buf, BloodCauldronRecipe recipe) {
            recipe.ingredient.write(buf);
            buf.writeItemStack(recipe.result);
        }
    }
}
