package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.BLRegistryKeys;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.auroali.sanguinisluxuria.common.registry.BLVampireAbilities;
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
        this.getOrCreateTagBuilder(BLTags.VampireAbilities.TELEPORT_RANGE)
          .add(BLVampireAbilities.TELEPORT_RANGE_1)
          .add(BLVampireAbilities.TELEPORT_RANGE_2);
    }
}
