package com.auroali.sanguinisluxuria.common.rituals;

import com.mojang.serialization.Codec;

public interface RitualType<T extends Ritual> {
    Codec<T> getCodec();

    static <T extends Ritual> RitualType<T> fromCodec(Codec<T> codec) {
        return () -> codec;
    }
}
