package com.auroali.sanguinisluxuria.common.blockentities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public interface ItemDisplayingBlockEntity {
    ItemStack getDisplayItem();

    int getDisplayTicks();

    Vec3d getDisplayOffset();
}
