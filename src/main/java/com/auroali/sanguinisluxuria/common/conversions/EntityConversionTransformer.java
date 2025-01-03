package com.auroali.sanguinisluxuria.common.conversions;

import com.google.gson.JsonElement;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Function;

public interface EntityConversionTransformer {
    void apply(Entity entity, NbtCompound nbtIn, NbtCompound nbtOut);

    JsonElement toJson();

    Serializer<?> getSerializer();

    class Serializer<T extends EntityConversionTransformer> {
        Function<JsonElement, T> fromJson;
        Function<T, JsonElement> toJson;

        public Serializer(Function<JsonElement, T> fromJson) {
            this.fromJson = fromJson;
            this.toJson = T::toJson;
        }
    }
}
