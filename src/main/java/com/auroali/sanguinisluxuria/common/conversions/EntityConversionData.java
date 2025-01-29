package com.auroali.sanguinisluxuria.common.conversions;

import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.InitializableBloodComponent;
import com.auroali.sanguinisluxuria.common.events.VampireConversionEvents;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntityConversionData {
    protected final ConversionType type;
    protected final EntityType<?> entity;
    protected final EntityType<?> target;
    protected final List<EntityConversionTransformer> transformers;
    protected final List<EntityConversionCondition> conditions;

    public EntityConversionData(ConversionType type, EntityType<?> entity, EntityType<?> target, List<EntityConversionTransformer> transformers, List<EntityConversionCondition> conditions) {
        this.type = type;
        this.entity = entity;
        this.target = target;
        this.transformers = transformers;
        this.conditions = conditions;
    }

    public EntityType<?> getEntity() {
        return this.entity;
    }

    public boolean testConditions(ConversionContext context) {
        for (EntityConversionCondition conditions : this.conditions) {
            if (!conditions.test(context))
                return false;
        }
        return true;
    }

    public void performConversion(ConversionContext context) {
        Entity entity = context.entity();
        World world = context.world();

        NbtCompound entityNbt = new NbtCompound();
        NbtCompound newNbt = new NbtCompound();
        entity.writeNbt(entityNbt);

        this.transformers.forEach(transformer -> transformer.apply(context, entityNbt, newNbt));
        Entity newEntity = this.type.apply(world, entity, this.target, newNbt);
        if (newEntity == entity) {
            VampireConversionEvents.AFTER_CONVERSION.invoker().afterConversion(context, newEntity);
            return;
        }

        if (newEntity.getType().isIn(BLTags.Entities.HAS_BLOOD) && BLEntityComponents.BLOOD_COMPONENT.isProvidedBy(newEntity) && BLEntityComponents.BLOOD_COMPONENT.isProvidedBy(entity)) {
            BloodComponent oldBlood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
            BloodComponent newBlood = BLEntityComponents.BLOOD_COMPONENT.get(newEntity);
            if (newBlood instanceof InitializableBloodComponent initializable)
                initializable.initializeBloodValues();

            newBlood.setBlood(Math.min(oldBlood.getBlood(), newBlood.getMaxBlood()));
        }

        world.spawnEntity(newEntity);
        entity.remove(Entity.RemovalReason.DISCARDED);

        VampireConversionEvents.AFTER_CONVERSION.invoker().afterConversion(context, newEntity);
    }

    public static EntityConversionData fromJson(JsonObject object) {
        if (!object.has("type"))
            throw new JsonParseException("Missing type field");
        if (!object.has("entity"))
            throw new JsonParseException("Missing entity field");
        if (!object.has("target"))
            throw new JsonParseException("Missing target field");
        if (!object.get("target").isJsonPrimitive() || !object.get("target").getAsJsonPrimitive().isString())
            throw new JsonParseException("Expected string for target, got " + object.get("target"));
        if (!object.get("entity").isJsonPrimitive() || !object.get("entity").getAsJsonPrimitive().isString())
            throw new JsonParseException("Expected string for entity, got " + object.get("entity"));

        ConversionType type = BLRegistries.CONVERSION_TYPES.get(Identifier.tryParse(object.get("type").getAsString()));
        if (type == null)
            throw new JsonParseException("Could not get type " + object.get("type"));

        EntityType<?> entity = Registries.ENTITY_TYPE.get(Identifier.tryParse(object.get("entity").getAsString()));
        EntityType<?> target = Registries.ENTITY_TYPE.get(Identifier.tryParse(object.get("target").getAsString()));
        // if the transformers field is present, parse it
        List<EntityConversionTransformer> transformers = object.has("transformers") && object.get("transformers").isJsonArray()
          ? parseTransformers(object.getAsJsonArray("transformers"))
          : Collections.emptyList();

        // if the conditions field is present, parse it
        List<EntityConversionCondition> conditions = object.has("conditions") && object.get("conditions").isJsonArray()
          ? parseConditions(object.getAsJsonArray("conditions"))
          : Collections.emptyList();

        return new EntityConversionData(type, entity, target, transformers, conditions);
    }

    public static List<EntityConversionCondition> parseConditions(JsonArray json) {
        List<EntityConversionCondition> conditions = new ArrayList<>(json.size());
        for (JsonElement element : json) {
            JsonObject conditionJson = element.getAsJsonObject();
            if (!conditionJson.has("type"))
                throw new JsonParseException("Condition missing type field");

            Identifier id = Identifier.tryParse(conditionJson.get("type").getAsString());
            if (id == null)
                throw new JsonParseException("Cannot parse id " + conditionJson.get("type"));

            EntityConversionCondition.Serializer<?> serializer = BLRegistries.CONVERSION_CONDITIONS.get(id);
            if (serializer == null)
                throw new JsonParseException("Failed to read condition type " + id);

            EntityConversionCondition condition = serializer.fromJson(conditionJson);
            conditions.add(condition);
        }
        return conditions;
    }

    public static List<EntityConversionTransformer> parseTransformers(JsonArray json) {
        List<EntityConversionTransformer> transformers = new ArrayList<>(json.size());
        for (JsonElement element : json) {
            JsonObject transformerJson = element.getAsJsonObject();
            if (!transformerJson.has("type"))
                throw new JsonParseException("Transformer missing type field");

            Identifier id = Identifier.tryParse(transformerJson.get("type").getAsString());
            if (id == null)
                throw new JsonParseException("Cannot parse id " + transformerJson.get("type"));

            EntityConversionTransformer.Serializer<?> serializer = BLRegistries.CONVERSION_TRANSFORMERS.get(id);
            if (serializer == null)
                throw new JsonParseException("Failed to read transformer type " + id);

            EntityConversionTransformer transformer = serializer.fromJson(transformerJson);
            transformers.add(transformer);
        }
        return transformers;
    }
}
