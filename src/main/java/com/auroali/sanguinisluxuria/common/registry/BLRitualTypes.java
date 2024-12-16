package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.rituals.*;
import net.minecraft.registry.Registry;

public class BLRitualTypes {
    public static final RitualType<ItemRitual> ITEM_RITUAL_TYPE = RitualType.fromCodec(ItemRitual.CODEC);
    public static final RitualType<VampireAbilityRitual> ABILITY_RITUAL_TYPE = RitualType.fromCodec(VampireAbilityRitual.CODEC);
    public static final RitualType<VampireAbilityResetRitual> ABILITY_RESET_RITUAL_TYPE = RitualType.fromCodec(VampireAbilityResetRitual.CODEC);
    public static final RitualType<?> ABILITY_REVEAL_RITUAL_TYPE = RitualType.fromCodec(AbilityRevealRitual.CODEC);

    public static void register() {
        Registry.register(BLRegistries.RITUAL_TYPES, BLResources.ITEM_RITUAL_TYPE, ITEM_RITUAL_TYPE);
        Registry.register(BLRegistries.RITUAL_TYPES, BLResources.ABILITY_RITUAL_TYPE, ABILITY_RITUAL_TYPE);
        Registry.register(BLRegistries.RITUAL_TYPES, BLResources.ABILITY_RESET_RITUAL_TYPE, ABILITY_RESET_RITUAL_TYPE);
        Registry.register(BLRegistries.RITUAL_TYPES, BLResources.ABILITY_REVEAL_RITUAL_TYPE, ABILITY_REVEAL_RITUAL_TYPE);
    }
}
