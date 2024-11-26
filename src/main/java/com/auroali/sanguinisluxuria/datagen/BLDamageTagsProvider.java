package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

public class BLDamageTagsProvider extends FabricTagProvider<DamageType> {

    public BLDamageTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_INVULNERABILITY)
          .addOptional(BLResources.BLOOD_DRAIN_DAMAGE_KEY);
        this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
          .addOptional(BLResources.BLESSED_WATER_DAMAGE_KEY);
        this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_EFFECTS)
          .addOptional(BLResources.BLESSED_WATER_DAMAGE_KEY);
        this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ENCHANTMENTS)
          .addOptional(BLResources.BLESSED_WATER_DAMAGE_KEY);
        this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_RESISTANCE)
          .addOptional(BLResources.BLESSED_WATER_DAMAGE_KEY);
        this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
          .addOptional(BLResources.BITE_DAMAGE_KEY);
        this.getOrCreateTagBuilder(DamageTypeTags.WITCH_RESISTANT_TO)
          .addOptional(BLResources.BLESSED_WATER_DAMAGE_KEY);
        this.getOrCreateTagBuilder(BLTags.DamageTypes.VAMPIRES_WEAK_TO)
          .forceAddTag(DamageTypeTags.WITCH_RESISTANT_TO)
          .forceAddTag(DamageTypeTags.IS_FIRE)
          .addOptional(BLResources.BLESSED_WATER_DAMAGE_KEY);
    }
}
