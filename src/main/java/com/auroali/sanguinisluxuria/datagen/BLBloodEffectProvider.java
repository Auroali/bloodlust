package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.auroali.sanguinisluxuria.datagen.builders.BloodDrainEffectBuilder;
import com.auroali.sanguinisluxuria.datagen.generators.SanguinisLuxuriaBloodEffectsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.entity.effect.StatusEffects;

import java.util.function.Consumer;

public class BLBloodEffectProvider extends SanguinisLuxuriaBloodEffectsProvider {
    public BLBloodEffectProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    protected void generateEffects(Consumer<BloodDrainEffectBuilder.Provider> exporter) {
        BloodDrainEffectBuilder.create(BLTags.Entities.TOXIC_BLOOD)
          .effect(StatusEffects.HUNGER, 0.9f)
          .effect(StatusEffects.WEAKNESS, 100, 0.73f)
          .offerTo(exporter, BLResources.id("toxic_blood"));
    }
}
