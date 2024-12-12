package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.BLRegistryKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class BLVampireAbiltyTagsProvider extends FabricTagProvider<VampireAbility> {
    public BLVampireAbiltyTagsProvider(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataGenerator, BLRegistryKeys.VAMPIRE_ABILITIES, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup args) {
    }
}
