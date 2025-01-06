package com.auroali.sanguinisluxuria.common.conversions.conditions;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionCondition;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.world.ServerWorld;

public class ConversionContextCondition implements EntityConversionCondition {
    final ConversionContext.Conversion conversion;
    final EntityPredicate predicate;

    public ConversionContextCondition(ConversionContext.Conversion conversion, EntityPredicate predicate) {
        this.conversion = conversion;
        this.predicate = predicate;
    }

    @Override
    public boolean test(ConversionContext context) {
        boolean result = true;
        if (this.conversion != null)
            result = this.conversion == context.conversion();
        if (this.predicate != null && context.world() instanceof ServerWorld world)
            result = result && this.predicate.test(world, context.entity().getPos(), context.entity());
        return result;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        if (this.conversion != null)
            object.addProperty("conversion", this.conversion.asString());
        if (this.predicate != null)
            object.add("predicate", this.predicate.toJson());
        return object;
    }

    @Override
    public EntityConversionCondition.Serializer<?> getSerializer() {
        return BLConversions.CONVERSION_CONTEXT_CONDITION;
    }

    public static ConversionContextCondition fromJson(JsonObject object) {
        ConversionContext.Conversion conversion = object.has("conversion") ? ConversionContext.Conversion.fromJson(object.get("conversion")) : null;
        EntityPredicate predicate = object.has("predicate") ? EntityPredicate.fromJson(object.get("predicate")) : null;
        if (conversion == null && predicate == null)
            throw new JsonParseException("Expected either a predicate or conversion field");
        return new ConversionContextCondition(conversion, predicate);
    }

    public static ConversionContextCondition converting() {
        return new ConversionContextCondition(ConversionContext.Conversion.CONVERTING, null);
    }

    public static ConversionContextCondition deconverting() {
        return new ConversionContextCondition(ConversionContext.Conversion.DECONVERTING, null);
    }

    public static ConversionContextCondition predicate(EntityPredicate predicate) {
        return new ConversionContextCondition(null, predicate);
    }

    public static ConversionContextConditionBuilder builder() {
        return new ConversionContextConditionBuilder();
    }

    public static class ConversionContextConditionBuilder {
        ConversionContext.Conversion conversion;
        EntityPredicate predicate;

        protected ConversionContextConditionBuilder() {
        }

        public ConversionContextConditionBuilder conversion(ConversionContext.Conversion conversion) {
            this.conversion = conversion;
            return this;
        }

        public ConversionContextConditionBuilder predicate(EntityPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public ConversionContextCondition build() {
            if (this.conversion == null && this.predicate == null)
                throw new IllegalStateException("ConversionContextCondition cannot be empty");
            return new ConversionContextCondition(this.conversion, this.predicate);
        }
    }
}
