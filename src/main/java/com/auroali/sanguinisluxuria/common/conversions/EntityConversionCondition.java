package com.auroali.sanguinisluxuria.common.conversions;

import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public interface EntityConversionCondition {
    boolean test(ConversionContext context);

    JsonObject toJson();

    Serializer<?> getSerializer();

    class Serializer<T extends EntityConversionCondition> {
        private final Function<JsonObject, T> fromJson;
        private final Function<T, JsonObject> toJson;

        public Serializer(Function<JsonObject, T> fromJson) {
            this.fromJson = fromJson;
            this.toJson = T::toJson;
        }

        public JsonObject toJson(T object) {
            JsonObject json = this.toJson.apply(object);
            Identifier id = BLRegistries.CONVERSION_CONDITIONS.getId(this);
            if (id == null)
                throw new IllegalStateException("Attempted to save condition using unregistered serializer!");
            json.addProperty("type", id.toString());
            return json;
        }

        public T fromJson(JsonObject object) {
            return this.fromJson.apply(object);
        }
    }
}
