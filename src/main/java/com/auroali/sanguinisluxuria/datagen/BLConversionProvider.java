package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.conversions.conditions.ConversionContextCondition;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.auroali.sanguinisluxuria.common.registry.BLEntities;
import com.auroali.sanguinisluxuria.datagen.builders.ConversionJsonBuilder;
import com.auroali.sanguinisluxuria.datagen.generators.SanguinisLuxuriaConversionsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.entity.EntityType;

import java.util.function.Consumer;

public class BLConversionProvider extends SanguinisLuxuriaConversionsProvider {
    public BLConversionProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    protected void generateConversions(Consumer<ConversionJsonBuilder.Provider> exporter) {
        ConversionJsonBuilder.create(EntityType.VILLAGER, BLEntities.VAMPIRE_VILLAGER)
          .type(BLConversions.SPAWN_TYPE)
          .condition(ConversionContextCondition.converting())
          .offerTo(exporter, Bloodlust.MODID);

        ConversionJsonBuilder.create(EntityType.PLAYER, EntityType.PLAYER)
          .type(BLConversions.SET_VAMPIRE_TYPE)
          .condition(ConversionContextCondition.converting())
          .offerTo(exporter, BLResources.id("convert_player_to_vampire"));

        ConversionJsonBuilder.create(EntityType.PLAYER, EntityType.PLAYER)
          .type(BLConversions.REVERT_VAMPIRE_TYPE)
          .condition(ConversionContextCondition.deconverting())
          .offerTo(exporter, BLResources.id("convert_vampire_to_player"));
    }
}
