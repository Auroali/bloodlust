package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.concurrent.CompletableFuture;

public class BLBiomeTagsProvider extends FabricTagProvider<Biome> {
    public BLBiomeTagsProvider(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataGenerator, RegistryKeys.BIOME, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup args) {
        this.getOrCreateTagBuilder(BLTags.Biomes.VAMPIRE_VILLAGER_SPAWN)
          .add(BiomeKeys.DARK_FOREST);
    }
}
