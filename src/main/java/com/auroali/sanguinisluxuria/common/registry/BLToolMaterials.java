package com.auroali.sanguinisluxuria.common.registry;

import com.google.common.base.Suppliers;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Util;

import java.util.function.Supplier;

public enum BLToolMaterials implements ToolMaterial {
    SILVER(MiningLevels.IRON, 240, 6.0F, 2.0F, 16, () -> Ingredient.ofItems(BLItems.SILVER_INGOT));
    private final int miningLevel;
    private final int durability;
    private final int enchantibility;
    private final float attackDamage;
    private final float miningSpeedMultiplier;
    private final Supplier<Ingredient> repairIngredient;

    BLToolMaterials(int miningLevel, int durability, float miningSpeedMultiplier, float attackDamage, int enchantibility, Supplier<Ingredient> repairIngredient) {
        this.miningLevel = miningLevel;
        this.durability = durability;
        this.enchantibility = enchantibility;
        this.attackDamage = attackDamage;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.repairIngredient = Suppliers.memoize(repairIngredient::get);
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return miningSpeedMultiplier;
    }

    @Override
    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return miningLevel;
    }

    @Override
    public int getEnchantability() {
        return enchantibility;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }
}
