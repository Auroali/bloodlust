package com.auroali.sanguinisluxuria.common.conversions.conditions;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionCondition;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionData;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class OrConversionCondition implements EntityConversionCondition {
    final List<EntityConversionCondition> conditions;

    public OrConversionCondition(List<EntityConversionCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(ConversionContext context) {
        for (EntityConversionCondition condition : this.conditions) {
            if (condition.test(context))
                return true;
        }
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        JsonArray conditionsArray = new JsonArray();
        for (EntityConversionCondition condition : this.conditions) {
            Identifier typeId = BLRegistries.CONVERSION_CONDITIONS.getId(condition.getSerializer());
            if (typeId == null)
                continue;

            JsonObject conditionJson = ((EntityConversionCondition.Serializer) condition.getSerializer()).toJson(condition);
            conditionsArray.add(conditionJson);
        }

        object.add("conditions", conditionsArray);
        return object;
    }

    @Override
    public Serializer<?> getSerializer() {
        return BLConversions.OR_CONDITION;
    }

    public static OrConversionCondition fromJson(JsonObject object) {
        if (!object.has("conditions"))
            throw new JsonParseException("Missing conditions field");
        if (!object.get("conditions").isJsonArray())
            throw new JsonParseException("Expected conditions to be a json array, got " + object.get("conditions"));

        List<EntityConversionCondition> conditions = EntityConversionData.parseConditions(object.getAsJsonArray("conditions"));
        return new OrConversionCondition(conditions);
    }

    public static OrConversionCondition create(EntityConversionCondition... conditions) {
        return new OrConversionCondition(Arrays.asList(conditions));
    }
}
