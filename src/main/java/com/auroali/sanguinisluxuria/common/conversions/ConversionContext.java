package com.auroali.sanguinisluxuria.common.conversions;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.entity.Entity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;

public record ConversionContext(World world, Entity entity, Conversion conversion) {
    public static ConversionContext from(Entity entity, Conversion conversion) {
        return new ConversionContext(entity.getWorld(), entity, conversion);
    }

    public enum Conversion implements StringIdentifiable {
        CONVERTING("converting"),
        DECONVERTING("deconverting");

        public static final com.mojang.serialization.Codec<Conversion> CODEC = StringIdentifiable.createCodec(Conversion::values);

        private final String name;

        Conversion(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public static Conversion fromJson(JsonElement element) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
                throw new JsonParseException("Expected a string for the conversion field");

            return switch (element.getAsString()) {
                case "converting" -> CONVERTING;
                case "deconverting" -> DECONVERTING;
                default -> throw new JsonParseException("Unknown conversion " + element.getAsString());
            };
        }
    }
}
