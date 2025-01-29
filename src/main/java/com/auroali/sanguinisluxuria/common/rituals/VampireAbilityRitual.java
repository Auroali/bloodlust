package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;

import java.util.List;

public record VampireAbilityRitual(VampireAbility ability) implements Ritual {
    public static final Codec<VampireAbilityRitual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      BLRegistries.VAMPIRE_ABILITIES.getCodec().fieldOf("ability").forGetter(VampireAbilityRitual::ability)
    ).apply(instance, VampireAbilityRitual::new));


    @Override
    public void onCompleted(RitualParameters parameters) {
        if (!VampireHelper.isVampire(parameters.target()))
            return;

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(parameters.target());
        VampireAbilityContainer abilities = vampire.getAbilties();
        if (abilities.hasAbility(this.ability) || !this.ability.testConditions(parameters.target(), vampire, abilities))
            // todo: add feedback
            return;

        vampire.unlockAbility(this.ability);
    }

    @Override
    public void appendTooltips(List<Text> tooltips) {
        tooltips.add(Text.translatable(this.ability.getTranslationKey()));
    }

    @Override
    public RitualType<?> getType() {
        return BLRitualTypes.ABILITY_RITUAL_TYPE;
    }
}
