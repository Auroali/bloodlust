package com.auroali.sanguinisluxuria.datagen.builders;

import com.auroali.sanguinisluxuria.common.conversions.ConversionType;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionCondition;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionTransformer;
import com.auroali.sanguinisluxuria.common.entities.VampireVillagerEntity;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConversionJsonBuilder {
    EntityType<?> from;
    EntityType<?> to;
    ConversionType type;
    List<EntityConversionTransformer> transformers;
    List<EntityConversionCondition> conditions;

    protected ConversionJsonBuilder(EntityType<?> from, EntityType<?> to) {
        this.from = from;
        this.to = to;
        this.transformers = new ArrayList<>();
        this.conditions = new ArrayList<>();
    }

    public static ConversionJsonBuilder create(EntityType<?> from, EntityType<?> to) {
        return new ConversionJsonBuilder(from, to);
    }

    public ConversionJsonBuilder type(ConversionType type) {
        this.type = type;
        return this;
    }

    public ConversionJsonBuilder condition(EntityConversionCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public void validate() {
        if (this.from == null || this.to == null)
            throw new IllegalStateException("Must specify both entities for a conversion");
        if (this.type == null)
            throw new IllegalStateException("Conversion Type cannot be null");
    }

    public void offerTo(Consumer<ConversionJsonBuilder.Provider> exporter, Identifier id) {
        this.validate();
        exporter.accept(new Provider(id, this.from, this.to, this.type, this.transformers, this.conditions));
    }

    public void offerTo(Consumer<ConversionJsonBuilder.Provider> exporter, String namespace) {
        this.offerTo(exporter, this.getIdFromEntities(namespace));
    }

    protected Identifier getIdFromEntities(String namespace) {
        Identifier fromId = Registries.ENTITY_TYPE.getId(this.from);
        Identifier toId = Registries.ENTITY_TYPE.getId(this.to);
        String path = fromId.getNamespace() + "_" + fromId.getPath() + "_to_" + toId.getNamespace() + "_" + toId.getPath();
        return new Identifier(namespace, path);
    }

    public static class Provider {
        private final Identifier id;
        private final EntityType<?> from;
        private final EntityType<?> to;
        private final ConversionType type;
        private final List<EntityConversionTransformer> transformers;
        private final List<EntityConversionCondition> conditions;

        public Provider(Identifier id, EntityType<?> from, EntityType<?> to, ConversionType type, List<EntityConversionTransformer> transformers, List<EntityConversionCondition> conditions) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.type = type;
            this.transformers = transformers;
            this.conditions = conditions;
        }

        public void serialize(JsonObject object) {
            Identifier fromId = Registries.ENTITY_TYPE.getId(this.from);
            Identifier toId = Registries.ENTITY_TYPE.getId(this.to);
            Identifier typeId = BLRegistries.CONVERSION_TYPES.getId(this.type);

            object.addProperty("entity", fromId.toString());
            object.addProperty("target", toId.toString());

            if (typeId == null)
                throw new IllegalStateException("Could not get type id");

            object.addProperty("type", typeId.toString());

            if (!this.transformers.isEmpty()) {
                JsonArray serializedTransformers = new JsonArray();
                this.transformers.forEach(transformer -> {
                    JsonObject transformerJson = transformer.toJson();
                    transformerJson.addProperty(
                      "type",
                      BLRegistries.CONVERSION_TRANSFORMERS.getId(transformer.getSerializer()).toString()
                    );
                    serializedTransformers.add(transformerJson);
                });
                object.add("transformers", serializedTransformers);
            }

            if (!this.conditions.isEmpty()) {
                JsonArray serializedConditions = new JsonArray();
                this.conditions.forEach(condition -> {
                    JsonObject conditionJson = condition.toJson();
                    conditionJson.addProperty(
                      "type",
                      BLRegistries.CONVERSION_CONDITIONS.getId(condition.getSerializer()).toString()
                    );
                    serializedConditions.add(conditionJson);
                });
                object.add("conditions", serializedConditions);
            }
        }

        public Identifier getId() {
            return this.id;
        }
    }
}
