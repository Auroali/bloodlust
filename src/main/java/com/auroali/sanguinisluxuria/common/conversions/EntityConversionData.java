package com.auroali.sanguinisluxuria.common.conversions;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.InitializableBloodComponent;
import com.auroali.sanguinisluxuria.common.events.VampireConversionEvents;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
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

        if (BLEntityComponents.BLOOD_COMPONENT.isProvidedBy(newEntity) && BLEntityComponents.BLOOD_COMPONENT.isProvidedBy(entity)) {
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
        ConversionType type = BLRegistries.CONVERSION_TYPES.get(Identifier.tryParse(object.get("type").getAsString()));
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
            Identifier id = Identifier.tryParse(element.getAsJsonObject().get("type").getAsString());
            if (id == null)
                continue;

            EntityConversionCondition.Serializer<?> serializer = BLRegistries.CONVERSION_CONDITIONS.get(id);
            if (serializer == null)
                continue;

            try {
                EntityConversionCondition condition = serializer.fromJson.apply(element.getAsJsonObject());
                conditions.add(condition);
            } catch (JsonParseException e) {
                Bloodlust.LOGGER.error("Failed to parse conversion condition of type {}", id);
                Bloodlust.LOGGER.error("Caught exception", e);
            }
        }
        return conditions;
    }

    public static List<EntityConversionTransformer> parseTransformers(JsonArray json) {
        List<EntityConversionTransformer> transformers = new ArrayList<>(json.size());
        for (JsonElement element : json) {
            Identifier id = Identifier.tryParse(element.getAsJsonObject().get("type").getAsString());
            if (id == null)
                continue;

            EntityConversionTransformer.Serializer<?> serializer = BLRegistries.CONVERSION_TRANSFORMERS.get(id);
            if (serializer == null)
                continue;
            try {
                EntityConversionTransformer transformer = serializer.fromJson.apply(element.getAsJsonObject());
                transformers.add(transformer);
            } catch (JsonParseException e) {
                Bloodlust.LOGGER.error("Failed to parse conversion transformer of type {}", id);
                Bloodlust.LOGGER.error("Caught exception", e);
            }
        }
        return transformers;
    }
}
