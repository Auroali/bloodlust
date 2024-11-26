package com.auroali.sanguinisluxuria.common.registry;

import com.google.common.base.Suppliers;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.function.Supplier;

public enum BLToolMaterials implements ToolMaterial {
    SILVER(MiningLevels.IRON, 240, 5.25F, 1.0F, 16, () -> Ingredient.fromTag(BLTags.Items.SILVER_INGOTS));
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
        return this.durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeedMultiplier;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return this.miningLevel;
    }

    @Override
    public int getEnchantability() {
        return this.enchantibility;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
