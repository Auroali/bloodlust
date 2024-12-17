package com.auroali.sanguinisluxuria.common.blood;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;

public record BloodDrainEffectInstance(StatusEffect effect, int duration, int amplifier, float chance) {
    public static final Codec<BloodDrainEffectInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(BloodDrainEffectInstance::effect),
      Codec.INT.optionalFieldOf("duration", 300).forGetter(BloodDrainEffectInstance::duration),
      Codec.INT.optionalFieldOf("amplifier", 0).forGetter(BloodDrainEffectInstance::amplifier),
      Codec.FLOAT.optionalFieldOf("chance", 1.f).forGetter(BloodDrainEffectInstance::chance)
    ).apply(instance, BloodDrainEffectInstance::new));

    public static BloodDrainEffectInstance merge(BloodDrainEffectInstance first, BloodDrainEffectInstance second) {
        if (first.effect() != second.effect()) {
            Bloodlust.LOGGER.warn("Cannot merge two BloodDrainEffectInstances with different effects {} and {}", Registries.STATUS_EFFECT.getId(first.effect()), Registries.STATUS_EFFECT.getId(second.effect()));
            return first;
        }
        if (second.amplifier() > first.amplifier()) {
            return second;
        }
        if (first.amplifier() == second.amplifier()) {
            return new BloodDrainEffectInstance(first.effect(), Math.max(first.duration(), second.duration()), first.amplifier(), Math.max(first.chance(), second.chance()));
        }
        return first;
    }
}
