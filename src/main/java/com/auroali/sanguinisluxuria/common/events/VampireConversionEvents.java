package com.auroali.sanguinisluxuria.common.events;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

public class VampireConversionEvents {
    /**
     * Invoked before any conversion conditions are checked. Allows cancelling a conversion on an entity.
     */
    public static final Event<AllowConversion> ALLOW_CONVERSION = EventFactory.createArrayBacked(AllowConversion.class, callbacks -> (context) -> {
        for (AllowConversion callback : callbacks) {
            if (!callback.allowConversion(context))
                return false;
        }
        return true;
    });

    /**
     * Invoked after a successful conversion. If the conversion spawns a new entity, the old one will have already been
     * removed. The original entity can be accessed via the context
     */
    public static final Event<AfterConversion> AFTER_CONVERSION = EventFactory.createArrayBacked(AfterConversion.class, callbacks -> (context, newEntity) -> {
        for (AfterConversion callback : callbacks) {
            callback.afterConversion(context, newEntity);
        }
    });

    @FunctionalInterface
    public interface AllowConversion {
        boolean allowConversion(ConversionContext context);
    }

    @FunctionalInterface
    public interface AfterConversion {
        void afterConversion(ConversionContext context, Entity newEntity);
    }
}
