package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

public class BLVampireVillagerTrades {
    public static final ImmutableMap<Integer, TradeOffers.Factory[]> TRADES = new ImmutableMap.Builder<Integer, TradeOffers.Factory[]>()
      .put(1, new TradeOffers.Factory[]{
        (entity, random) -> new TradeOffer(new ItemStack(Items.EMERALD, 2), ItemStack.EMPTY, BloodStorageItem.setStoredBlood(new ItemStack(BLItems.BLOOD_BOTTLE), BloodConstants.BLOOD_PER_BOTTLE), 4, 4, 1),
        new TradeOffers.SellItemFactory(BLItems.MASK_1, 3, 1, 1, 2),
        new TradeOffers.SellItemFactory(BLItems.MASK_2, 3, 1, 1, 2),
        new TradeOffers.SellItemFactory(BLItems.MASK_3, 3, 1, 1, 2),
        new TradeOffers.BuyForOneEmeraldFactory(BLItems.TWISTED_BLOOD, 1, 3, 4)
      })
      .build();
}
