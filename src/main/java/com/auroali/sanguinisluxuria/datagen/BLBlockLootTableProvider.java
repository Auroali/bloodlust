package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;

public class BLBlockLootTableProvider extends FabricBlockLootTableProvider {
    public BLBlockLootTableProvider(FabricDataOutput dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generate() {
        addDrop(BLBlocks.ALTAR);
        addDrop(BLBlocks.PEDESTAL);
        addDrop(BLBlocks.BLOOD_CAULDRON, Items.CAULDRON);
        addDrop(BLBlocks.SILVER_ORE, new LootTable.Builder()
          .pool(new LootPool.Builder()
            .rolls(ConstantLootNumberProvider.create(1))
            .with(AlternativeEntry.builder(
                ItemEntry.builder(BLBlocks.SILVER_ORE)
                  .conditionally(MatchToolLootCondition.builder(
                      ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.atLeast(1)))
                    )
                  ), ItemEntry.builder(BLItems.RAW_SILVER)
                  .apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))
                  .apply(ExplosionDecayLootFunction.builder())
              )
            )));
        addDrop(BLBlocks.DEEPSLATE_SILVER_ORE, new LootTable.Builder()
          .pool(new LootPool.Builder()
            .rolls(ConstantLootNumberProvider.create(1))
            .with(AlternativeEntry.builder(
                ItemEntry.builder(BLBlocks.DEEPSLATE_SILVER_ORE)
                  .conditionally(MatchToolLootCondition.builder(
                      ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.atLeast(1)))
                    )
                  ), ItemEntry.builder(BLItems.RAW_SILVER)
                  .apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))
                  .apply(ExplosionDecayLootFunction.builder())
              )
            )));
        addDrop(BLBlocks.SILVER_BLOCK);
        addDrop(BLBlocks.RAW_SILVER_BLOCK);

        addDrop(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG);
        addDrop(BLBlocks.HUNGRY_DECAYED_LOG);
        addDrop(BLBlocks.DECAYED_LOG);
        addDrop(BLBlocks.DECAYED_WOOD);
        addDrop(BLBlocks.STRIPPED_DECAYED_LOG);
        addDrop(BLBlocks.STRIPPED_DECAYED_WOOD);
        addDrop(BLBlocks.DECAYED_TWIGS);
        addDrop(BLBlocks.GRAFTED_SAPLING);

        addDrop(BLBlocks.DECAYED_PRESSURE_PLATE);
        addDrop(BLBlocks.SILVER_PRESSURE_PLATE);
    }
}
