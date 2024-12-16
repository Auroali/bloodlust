package com.auroali.sanguinisluxuria.common.rituals;

import net.minecraft.item.ItemStack;

/**
 * Represents a ritual that creates an item
 */
public interface ItemCreatingRitual {
    ItemStack getOutput();
}
