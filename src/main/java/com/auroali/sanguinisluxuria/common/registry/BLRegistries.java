package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
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

    // called to cause the class the load
    @SuppressWarnings({ "unused", "EmptyMethod" })
    public static void init() {
    }
}
