package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.text.Text;

import java.util.List;

public interface Ritual {
    Codec<Ritual> RITUAL_CODEC = BLRegistries.RITUAL_TYPES
      .getCodec()
      .dispatch("type", Ritual::getType, RitualType::getCodec);

    void onCompleted(RitualParameters parameters);

    RitualType<?> getType();

    default void appendTooltips(List<Text> tooltips) {
    }
}
