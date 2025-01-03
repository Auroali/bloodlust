package com.auroali.sanguinisluxuria.common.conversions;

import com.google.gson.JsonElement;
import net.minecraft.entity.Entity;

import java.util.function.Function;

public interface EntityConversionCondition {
    boolean test(Entity entity);

    JsonElement toJson();

    EntityConversionTransformer.Serializer<?> getSerializer();

    class Serializer<T extends EntityConversionCondition> {
        Function<JsonElement, T> fromJson;
        Function<T, JsonElement> toJson;

        public Serializer(Function<JsonElement, T> fromJson) {
            this.fromJson = fromJson;
            this.toJson = T::toJson;
        }
    }
}
