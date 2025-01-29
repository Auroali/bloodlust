package com.auroali.sanguinisluxuria.common.conversions;

import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public interface EntityConversionTransformer {
    void apply(ConversionContext context, NbtCompound nbtIn, NbtCompound nbtOut);

    JsonObject toJson();

    Serializer<?> getSerializer();

    class Serializer<T extends EntityConversionTransformer> {
        private final Function<JsonObject, T> fromJson;
        private final Function<T, JsonObject> toJson;

        public Serializer(Function<JsonObject, T> fromJson) {
            this.fromJson = fromJson;
            this.toJson = T::toJson;
        }

        public JsonObject toJson(T object) {
            JsonObject json = this.toJson.apply(object);
            Identifier id = BLRegistries.CONVERSION_TRANSFORMERS.getId(this);
            if (id == null)
                throw new IllegalStateException("Attempted to save transformer using unregistered serializer!");
            json.addProperty("type", id.toString());
            return json;
        }

        public T fromJson(JsonObject object) {
            return this.fromJson.apply(object);
        }
    }
}
