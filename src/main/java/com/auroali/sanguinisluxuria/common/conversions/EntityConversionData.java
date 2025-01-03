package com.auroali.sanguinisluxuria.common.conversions;

import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.InitializableBloodComponent;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityConversionData {
    protected ConversionType type;
    protected EntityType<?> entity;
    protected EntityType<?> target;
    protected List<EntityConversionTransformer> transformers;
    protected List<EntityConversionCondition> conditions;

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

    public boolean testConditions(Entity entity) {
        for (EntityConversionCondition conditions : this.conditions) {
            if (!conditions.test(entity))
                return false;
        }
        return true;
    }

    public void performConversion(World world, Entity entity) {
        NbtCompound entityNbt = new NbtCompound();
        NbtCompound newNbt = new NbtCompound();
        entity.writeNbt(entityNbt);

        this.transformers.forEach(transformer -> transformer.apply(entity, entityNbt, newNbt));
        Entity newEntity = this.type.apply(world, entity, this.target, newNbt);
        if (newEntity == entity)
            return;

        if (BLEntityComponents.BLOOD_COMPONENT.isProvidedBy(newEntity) && BLEntityComponents.BLOOD_COMPONENT.isProvidedBy(entity)) {
            BloodComponent oldBlood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
            BloodComponent newBlood = BLEntityComponents.BLOOD_COMPONENT.get(newEntity);
            if (newBlood instanceof InitializableBloodComponent initializeable)
                initializeable.initializeBloodValues();

            newBlood.setBlood(Math.min(oldBlood.getBlood(), newBlood.getMaxBlood()));
        }

        world.spawnEntity(newEntity);
    }

    public static EntityConversionData fromJson(JsonObject object) {
        ConversionType type = BLRegistries.CONVERSION_TYPES.get(Identifier.tryParse(object.get("type").getAsString()));
        EntityType<?> entity = Registries.ENTITY_TYPE.get(Identifier.tryParse(object.get("entity").getAsString()));
        EntityType<?> target = Registries.ENTITY_TYPE.get(Identifier.tryParse(object.get("target").getAsString()));
        List<EntityConversionTransformer> transformers = new ArrayList<>();
        List<EntityConversionCondition> conditions = new ArrayList<>();

        if (object.has("transformers")) {
            JsonArray transformersJson = object.getAsJsonArray("transformers");
            for (JsonElement element : transformersJson) {
                Identifier id = Identifier.tryParse(element.getAsJsonObject().get("type").getAsString());
                if (id == null)
                    continue;

                EntityConversionTransformer.Serializer<?> serializer = BLRegistries.CONVERSION_TRANSFORMERS.get(id);
                if (serializer == null)
                    continue;
                EntityConversionTransformer transformer = serializer.fromJson.apply(element);
                transformers.add(transformer);
            }
        }

        if (object.has("conditions")) {
            JsonArray conditionsJson = object.getAsJsonArray("conditions");
            for (JsonElement element : conditionsJson) {
                Identifier id = Identifier.tryParse(element.getAsJsonObject().get("type").getAsString());
                if (id == null)
                    continue;

                EntityConversionCondition.Serializer<?> serializer = BLRegistries.CONVERSION_CONDITIONS.get(id);
                if (serializer == null)
                    continue;
                EntityConversionCondition condition = serializer.fromJson.apply(element);
                conditions.add(condition);
            }
        }

        return new EntityConversionData(type, entity, target, transformers, conditions);
    }
}
