package com.auroali.sanguinisluxuria.common.items;

import com.auroali.sanguinisluxuria.BLResources;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public interface BloodStorageItem {
    String BLOOD_KEY = "Blood";
    String CURRENT_BLOOD_KEY = "CurrentBlood";
    String MAX_BLOOD_KEY = "MaxBlood";

    default void setBlood(ItemStack stack, int blood) {
        getOrCreateBloodTag(stack)
          .putInt(CURRENT_BLOOD_KEY, Math.min(blood, this.getMaxBlood(stack)));
    }

    default int getBlood(ItemStack stack) {
        if (!stack.hasNbt() || !stack.getNbt().contains(BLOOD_KEY, NbtElement.COMPOUND_TYPE))
            return 0;

        return stack.getSubNbt(BLOOD_KEY).getInt(CURRENT_BLOOD_KEY);
    }

    default int getMaxBlood(ItemStack stack) {
        if (!stack.hasNbt() || !stack.getNbt().contains(BLOOD_KEY, NbtElement.COMPOUND_TYPE))
            return 0;

        return stack.getSubNbt(BLOOD_KEY).getInt(MAX_BLOOD_KEY);
    }

    default ItemStack createEmptyItem(ItemStack stack) {
        return stack;
    }

    default boolean canFill() {
        return true;
    }

    default boolean canDrain() {
        return true;
    }

    int getDefaultMaxBlood();

    static NbtCompound getOrCreateBloodTag(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains(BLOOD_KEY, NbtElement.COMPOUND_TYPE))
            return stack.getSubNbt(BLOOD_KEY);

        NbtCompound bloodTag = stack.getOrCreateSubNbt(BLOOD_KEY);
        bloodTag.putInt(CURRENT_BLOOD_KEY, 0);
        bloodTag.putInt(MAX_BLOOD_KEY, stack.getItem() instanceof BloodStorageItem item ? item.getDefaultMaxBlood() : 0);
        return bloodTag;
    }

    static int getItemBlood(ItemStack stack) {
        if (stack.getItem() instanceof BloodStorageItem item)
            return item.getBlood(stack);
        return 0;
    }

    static int getItemMaxBlood(ItemStack stack) {
        if (stack.getItem() instanceof BloodStorageItem item)
            return item.getMaxBlood(stack);
        return 0;
    }

    static ItemStack setItemBlood(ItemStack stack, int blood) {
        if (stack.getItem() instanceof BloodStorageItem item)
            item.setBlood(stack, blood);
        return stack;
    }

    static boolean decrementItemBlood(ItemStack stack, int amount) {
        if (!stack.hasNbt() || !stack.getNbt().contains(BLOOD_KEY, NbtElement.COMPOUND_TYPE))
            getOrCreateBloodTag(stack);

        int blood = getItemBlood(stack);
        if (blood - amount < 0)
            return false;

        setItemBlood(stack, blood - amount);
        return true;
    }

    static boolean incrementItemBlood(ItemStack stack, int amount) {
        if (!stack.hasNbt() || !stack.getNbt().contains(BLOOD_KEY, NbtElement.COMPOUND_TYPE))
            getOrCreateBloodTag(stack);

        int blood = getItemBlood(stack);
        int maxBlood = getItemMaxBlood(stack);
        if (blood + amount > maxBlood) {
            return false;
        }

        setItemBlood(stack, blood + amount);
        return true;
    }

    static ItemStack createStack(Item item, int blood) {
        ItemStack stack = new ItemStack(item);
        return setItemBlood(stack, blood);
    }

    static ItemStack createStack(Item item) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof BloodStorageItem bloodStorage)
            return setItemBlood(new ItemStack(item), bloodStorage.getDefaultMaxBlood());
        return stack;
    }

    static ItemStack createEmptyStackFor(ItemStack stack) {
        return stack.getItem() instanceof BloodStorageItem item ? item.createEmptyItem(stack) : stack;
    }

    static boolean isItemFillable(ItemStack stack) {
        if (stack.getItem() instanceof BloodStorageItem item)
            return item.canFill();
        return false;
    }

    static boolean isItemDrainable(ItemStack stack) {
        if (stack.getItem() instanceof BloodStorageItem item)
            return item.canDrain();
        return false;
    }

    static void registerModelPredicate(Item item) {
        if (item instanceof BloodStorageItem bloodStorage) {
            ModelPredicateProviderRegistry.register(BLResources.BLOOD_STORAGE_ITEM_MODEL_PREDICATE, (stack, world, entity, seed) -> {
                int blood = bloodStorage.getBlood(stack);
                int maxBlood = bloodStorage.getMaxBlood(stack);

                return (float) blood / maxBlood;
            });
        }
    }
}
