package com.auroali.sanguinisluxuria.common.conversions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public interface EntityConversionCondition {
    boolean test(ConversionContext context);

    JsonObject toJson();

    Serializer<?> getSerializer();

    class Serializer<T extends EntityConversionCondition> {
        Function<JsonObject, T> fromJson;
        Function<T, JsonElement> toJson;

        public Serializer(Function<JsonObject, T> fromJson) {
            this.fromJson = fromJson;
            this.toJson = T::toJson;
        }
    }
}
