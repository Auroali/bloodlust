package com.auroali.sanguinisluxuria.common.items;

import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import net.minecraft.item.ItemStack;

public class BloodBagItem extends DrinkableBloodStorageItem {
    public BloodBagItem(Settings settings) {
        super(settings, 10 * BloodConstants.BLOOD_PER_BOTTLE);
    }

    @Override
    public int getMaxBloodForDrinking(ItemStack stack) {
        return 8;
    }
}
