package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLAdvancementCriterion;
import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.mojang.serialization.Codec;
import net.minecraft.server.network.ServerPlayerEntity;

public class VampireAbilityResetRitual implements Ritual {
    public static final VampireAbilityResetRitual INSTANCE = new VampireAbilityResetRitual();
    public static final Codec<VampireAbilityResetRitual> CODEC = Codec.unit(INSTANCE);

    protected VampireAbilityResetRitual() {
    }

    @Override
    public void onCompleted(RitualParameters parameters) {
        if (!VampireHelper.isVampire(parameters.initiator()))
            return;

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(parameters.initiator());
        VampireAbilityContainer abilities = vampire.getAbilties();
        for (VampireAbility ability : abilities) {
            ability.onAbilityRemoved(parameters.initiator(), vampire);
            abilities.removeAbility(ability);
            if (parameters.initiator() instanceof ServerPlayerEntity player)
                BLAdvancementCriterion.RESET_ABILITIES.trigger(player);
        }

        BLEntityComponents.VAMPIRE_COMPONENT.sync(parameters.initiator());
    }

    @Override
    public RitualType<?> getType() {
        return BLRitualTypes.ABILITY_RESET_RITUAL_TYPE;
    }
}
