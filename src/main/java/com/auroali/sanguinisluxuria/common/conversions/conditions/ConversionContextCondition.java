package com.auroali.sanguinisluxuria.common.conversions.conditions;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionCondition;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.google.gson.JsonObject;

public class ConversionContextCondition implements EntityConversionCondition {
    ConversionContext.Conversion conversion;

    public ConversionContextCondition(ConversionContext.Conversion conversion) {
        this.conversion = conversion;
    }

    @Override
    public boolean test(ConversionContext context) {
        return context.conversion() == this.conversion;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("conversion", this.conversion.asString());
        return object;
    }

    @Override
    public EntityConversionCondition.Serializer<?> getSerializer() {
        return BLConversions.CONVERSION_CONTEXT_CONDITION;
    }

    public static ConversionContextCondition fromJson(JsonObject object) {
        ConversionContext.Conversion conversion = ConversionContext.Conversion.fromJson(object.get("conversion"));
        return new ConversionContextCondition(conversion);
    }

    public static ConversionContextCondition converting() {
        return new ConversionContextCondition(ConversionContext.Conversion.CONVERTING);
    }

    public static ConversionContextCondition deconverting() {
        return new ConversionContextCondition(ConversionContext.Conversion.DECONVERTING);
    }
}
