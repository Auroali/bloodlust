package com.auroali.sanguinisluxuria.common.conversions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Function;

public interface EntityConversionTransformer {
    void apply(ConversionContext context, NbtCompound nbtIn, NbtCompound nbtOut);

    JsonObject toJson();

    Serializer<?> getSerializer();

    class Serializer<T extends EntityConversionTransformer> {
        Function<JsonObject, T> fromJson;
        Function<T, JsonElement> toJson;

        public Serializer(Function<JsonObject, T> fromJson) {
            this.fromJson = fromJson;
            this.toJson = T::toJson;
        }
    }
}
