package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.rituals.RitualType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class BLRegistryKeys {
    public static final RegistryKey<Registry<VampireAbility>> VAMPIRE_ABILITIES = RegistryKey.ofRegistry(BLResources.VAMPIRE_ABILITY_REGISTRY_ID);
    public static final RegistryKey<Registry<RitualType<?>>> RITUAL_TYPES = RegistryKey.ofRegistry(BLResources.RITUAL_TYPE_REGISTRY_ID);
}
