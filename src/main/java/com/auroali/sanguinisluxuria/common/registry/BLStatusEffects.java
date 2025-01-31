package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.statuseffects.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BLStatusEffects {
    public static final StatusEffect BLOOD_SICKNESS = new BloodSicknessEffect(StatusEffectCategory.HARMFUL, 0xFF3A0000);
    public static final StatusEffect BLESSED_WATER = new BlessedWaterEffect(StatusEffectCategory.BENEFICIAL, 0x7AD0E6);
    public static final StatusEffect BLOOD_PROTECTION = new BloodProtectionEffect(StatusEffectCategory.BENEFICIAL, 0x7AD0E6);
    public static final StatusEffect BLEEDING = new BleedingEffect(StatusEffectCategory.HARMFUL, 0xFF6C0000);
    public static final StatusEffect BLOOD_LUST = new BloodLustEffect(StatusEffectCategory.HARMFUL, 0xFFD52600);

    public static final Potion BLESSED_WATER_POTION = new Potion(new StatusEffectInstance(BLESSED_WATER, 0, 0));
    public static final Potion BLESSED_WATER_POTION_TWO = new Potion(new StatusEffectInstance(BLESSED_WATER, 0, 1));
    public static final Potion BLOOD_LUST_POTION = new Potion(new StatusEffectInstance(BLOOD_LUST, 1800, 0));

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT, BLResources.BLOOD_SICKNESS_ID, BLOOD_SICKNESS);
        Registry.register(Registries.STATUS_EFFECT, BLResources.BLESSED_WATER_ID, BLESSED_WATER);
        Registry.register(Registries.STATUS_EFFECT, BLResources.BLOOD_PROTECTION_ID, BLOOD_PROTECTION);
        Registry.register(Registries.STATUS_EFFECT, BLResources.BLEEDING_ID, BLEEDING);
        Registry.register(Registries.STATUS_EFFECT, BLResources.BLOOD_LUST_ID, BLOOD_LUST);

        Registry.register(Registries.POTION, BLResources.BLESSED_WATER_ID, BLESSED_WATER_POTION);
        Registry.register(Registries.POTION, BLResources.BLESSED_WATER_TWO_ID, BLESSED_WATER_POTION_TWO);
        Registry.register(Registries.POTION, BLResources.BLOOD_LUST_ID, BLOOD_LUST_POTION);
        BrewingRecipeRegistry.registerPotionRecipe(BLESSED_WATER_POTION, Items.GLOWSTONE_DUST, BLESSED_WATER_POTION_TWO);
        BrewingRecipeRegistry.registerPotionRecipe(BLOOD_LUST_POTION, BLItems.BLOOD_PETAL, Potions.MUNDANE);
    }
}
