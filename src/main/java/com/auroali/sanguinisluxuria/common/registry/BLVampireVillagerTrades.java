package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.google.common.collect.ImmutableMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BLVampireVillagerTrades {
    public static final ImmutableMap<Integer, TradeOffers.Factory[]> TRADES = new ImmutableMap.Builder<Integer, TradeOffers.Factory[]>()
      .put(1, new TradeOffers.Factory[]{
        (entity, random) -> new TradeOffer(new ItemStack(Items.EMERALD, 2), ItemStack.EMPTY, BloodStorageItem.createStack(BLItems.BLOOD_BOTTLE), 4, 4, 1),
        new TradeOffers.SellItemFactory(BLItems.MASK_1, 3, 1, 1, 2),
        new TradeOffers.SellItemFactory(BLItems.MASK_2, 3, 1, 1, 2),
        new TradeOffers.SellItemFactory(BLItems.MASK_3, 3, 1, 1, 2),
        new TradeOffers.BuyForOneEmeraldFactory(BLItems.TWISTED_BLOOD, 1, 3, 4),
        new TradeOffers.BuyForOneEmeraldFactory(BLItems.BLOOD_PETAL, 1, 3, 1),
        new VampireEnchantedBookFactory(3)
      })
      .put(2, new TradeOffers.Factory[]{
        new SellPotionFactory(Items.POTION, Potions.FIRE_RESISTANCE, 16, 1, 1, 4),
        new TradeOffers.BuyForOneEmeraldFactory(BLBlocks.GRAFTED_SAPLING.asItem(), 1, 1, 4)
      })
      .build();

    public static class SellPotionFactory implements TradeOffers.Factory {
        final int cost;
        final int count;
        final int maxUses;
        final int experience;
        final Item potionItem;
        final Potion potion;

        public SellPotionFactory(Item item, Potion potion, int price, int count, int maxUses, int experience) {
            this.potionItem = item;
            this.potion = potion;
            this.cost = price;
            this.maxUses = maxUses;
            this.count = count;
            this.experience = experience;
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(new ItemStack(Items.EMERALD, this.cost), PotionUtil.setPotion(new ItemStack(this.potionItem, this.count), this.potion), this.maxUses, this.experience, 0.2f);
        }
    }

    // modified version of TradeOffers.EnchantBook that only offers vampire enchantments
    public static class VampireEnchantedBookFactory implements TradeOffers.Factory {
        final int experience;

        public VampireEnchantedBookFactory(int experience) {
            this.experience = experience;
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            List<Enchantment> validEnchantments = BLTags.getAllEntriesInTag(BLTags.Enchantments.VAMPIRE_MERCHANT_OFFERS, Registries.ENCHANTMENT);
            Enchantment enchantment = validEnchantments.get(random.nextInt(validEnchantments.size()));
            int level = MathHelper.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
            ItemStack book = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, level));
            int cost = 2 + random.nextInt(5 + level * 10) + 3 * level;
            if (enchantment.isTreasure())
                cost *= 2;


            return new TradeOffer(new ItemStack(Items.EMERALD, Math.min(cost, 64)), new ItemStack(Items.BOOK), book, 12, this.experience, 0.2f);
        }
    }
}
