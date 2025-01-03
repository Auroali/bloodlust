package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.conversions.ConversionType;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionCondition;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionTransformer;
import com.auroali.sanguinisluxuria.common.rituals.RitualType;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;

public class BLRegistries {
    public static final Registry<VampireAbility> VAMPIRE_ABILITIES = FabricRegistryBuilder.from(
      new SimpleRegistry<>(
        BLRegistryKeys.VAMPIRE_ABILITIES,
        Lifecycle.stable(),
        true
      )
    ).buildAndRegister();
    public static final Registry<RitualType<?>> RITUAL_TYPES = FabricRegistryBuilder
      .createSimple(BLRegistryKeys.RITUAL_TYPES)
      .buildAndRegister();
    public static final Registry<ConversionType> CONVERSION_TYPES = FabricRegistryBuilder
      .createSimple(BLRegistryKeys.CONVERSION_TYPES)
      .buildAndRegister();
    public static final Registry<EntityConversionTransformer.Serializer<?>> CONVERSION_TRANSFORMERS = FabricRegistryBuilder
      .createSimple(BLRegistryKeys.CONVERSION_TRANSFORMERS)
      .buildAndRegister();
    public static final Registry<EntityConversionCondition.Serializer<?>> CONVERSION_CONDITIONS = FabricRegistryBuilder
      .createSimple(BLRegistryKeys.CONVERSION_CONDITIONS)
      .buildAndRegister();

    // called to cause the class the load
    @SuppressWarnings({ "unused", "EmptyMethod" })
    public static void init() {
    }
}
