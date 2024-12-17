package com.auroali.sanguinisluxuria.common.events;

import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class VampireSunEvents {
    public static final Event<VampireSunTimeCallback> MODIFY_SUN_TIME = EventFactory.createArrayBacked(VampireSunTimeCallback.class, callbacks -> (entity, vampire, value) -> {
        int timeInSun = value;
        for (VampireSunTimeCallback callback : callbacks) {
            timeInSun = callback.getMaxTimeInSun(entity, vampire, timeInSun);
        }
        return timeInSun;
    });

    public static final Event<VampireCanBurnCallback> CAN_BURN = EventFactory.createArrayBacked(VampireCanBurnCallback.class, callbacks -> (world, entity, vampire) -> {
        for (VampireCanBurnCallback callback : callbacks) {
            if (!callback.canBurn(world, entity, vampire))
                return false;
        }
        return true;
    });

    @FunctionalInterface
    public interface VampireSunTimeCallback {
        int getMaxTimeInSun(LivingEntity entity, VampireComponent vampire, int value);
    }

    @FunctionalInterface
    public interface VampireCanBurnCallback {
        boolean canBurn(World world, LivingEntity entity, VampireComponent vampire);
    }
}
