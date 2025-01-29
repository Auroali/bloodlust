package com.auroali.sanguinisluxuria.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EmptyingDrinkableBloodItem extends DrinkableBloodItem {
    private final EmptyItemFactory itemFactory;

    /**
     * Constructs a DrinkableBloodItem
     *
     * @param maxBlood    the maximum amount of blood this item can hold (by default)
     * @param itemFactory the factory for creating the empty item stack
     * @param settings    the item's settings. It's recommended you set the food component to the blood one
     * @see DrinkableBloodItem#BLOOD_FOOD_COMPONENT
     */
    public EmptyingDrinkableBloodItem(int maxBlood, EmptyItemFactory itemFactory, Settings settings) {
        super(maxBlood, settings);
        this.itemFactory = itemFactory;
    }

    /**
     * Constructs a DrinkableBloodItem
     *
     * @param maxBlood the maximum amount of blood this item can hold (by default)
     * @param item     the item this item turns into upon being emptied
     * @param settings the item's settings. It's recommended you set the food component to the blood one
     * @see DrinkableBloodItem#BLOOD_FOOD_COMPONENT
     */
    public EmptyingDrinkableBloodItem(int maxBlood, Item item, Settings settings) {
        this(maxBlood, (stack, blood, maxBlood1) -> new ItemStack(item, stack.getCount()), settings);
    }

    @Override
    public ItemStack createEmptyItem(ItemStack stack) {
        return this.itemFactory.createEmpty(stack, BloodStorageItem.getItemBlood(stack), BloodStorageItem.getItemMaxBlood(stack));
    }

    public interface EmptyItemFactory {
        ItemStack createEmpty(ItemStack stack, int blood, int maxBlood);
    }
}
