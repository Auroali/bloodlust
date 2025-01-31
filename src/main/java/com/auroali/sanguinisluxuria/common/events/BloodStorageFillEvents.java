package com.auroali.sanguinisluxuria.common.events;

import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class BloodStorageFillEvents {
    /**
     * Can be used to transform non-blood storing items into a blood storing item.
     * The output stack will only be used if its item implements {@link BloodStorageItem}
     */
    public static Event<TransformStackCallback> TRANSFORM_STACK = EventFactory.createArrayBacked(TransformStackCallback.class, callbacks -> (entity, stack) -> {
        for (TransformStackCallback callback : callbacks) {
            ItemStack transformedStack = callback.createFrom(entity, stack);
            if (!transformedStack.isEmpty() && transformedStack.getItem() instanceof BloodStorageItem)
                return transformedStack;
        }

        return ItemStack.EMPTY;
    });

    /**
     * Called after the blood storage item check. Can be used
     * with {@link BloodStorageFillEvents#TRANSFORM_STACK} to create
     * custom regular item -> blood storage item rules
     */
    public static Event<AllowItemEvent> ALLOW_ITEM = EventFactory.createArrayBacked(AllowItemEvent.class, callbacks -> (entity, stack) -> {
        for (AllowItemEvent callback : callbacks) {
            if (callback.allowItem(entity, stack))
                return true;
        }

        return false;
    });

    @FunctionalInterface
    public interface TransformStackCallback {
        ItemStack createFrom(LivingEntity entity, ItemStack stack);
    }

    @FunctionalInterface
    public interface AllowItemEvent {
        boolean allowItem(LivingEntity entity, ItemStack stack);
    }
}
