package com.auroali.sanguinisluxuria;

import com.auroali.sanguinisluxuria.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BloodlustDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BLLangProvider::new);
        pack.addProvider(BLEntityTagsProvider::new);
        pack.addProvider(BLItemTagsProvider::new);
        pack.addProvider(BLModelProvider::new);
        pack.addProvider(BLRecipeProvider::new);
        pack.addProvider(BLBlockTagsProvider::new);
        pack.addProvider(BLBiomeTagsProvider::new);
        pack.addProvider(BLVampireAbiltyTagsProvider::new);
        pack.addProvider(BLAdvancementsProvider::new);
        pack.addProvider(BLBlockLootTableProvider::new);
        pack.addProvider(BLEntityLootTableProvider::new);
        pack.addProvider(BLDamageTagsProvider::new);
        pack.addProvider(BLEnchantmentTagsProvider::new);
        pack.addProvider(BLConversionProvider::new);
    }
}
