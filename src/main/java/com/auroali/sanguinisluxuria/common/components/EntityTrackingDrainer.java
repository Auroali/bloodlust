package com.auroali.sanguinisluxuria.common.components;

import net.minecraft.entity.Entity;

public interface EntityTrackingDrainer {
    void setLastDrained(Entity entity);

    Entity getLastDrained();
}
