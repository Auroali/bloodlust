package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BLEntityLootTableProvider extends SimpleFabricLootTableProvider {
    public BLEntityLootTableProvider(FabricDataOutput dataGenerator) {
        super(dataGenerator, LootContextTypes.ENTITY);
    }

    @Override
    public void accept(BiConsumer<Identifier, LootTable.Builder> identifierBuilderBiConsumer) {
        identifierBuilderBiConsumer.accept(BLResources.id("entities/vampire_villager"), LootTable.builder()
          .type(LootContextTypes.ENTITY)
          .pool(LootPool.builder()
            .rolls(ConstantLootNumberProvider.create(1))
            .with(ItemEntry.builder(BLItems.MASK_1))
            .with(ItemEntry.builder(BLItems.BLOOD_BOTTLE)
              .apply(this.createNbtLootFunc(c -> c.putInt("StoredBlood", 1)))
            )
            .build()
          )
        );
    }

    public ConditionalLootFunction.Builder<?> createNbtLootFunc(Consumer<NbtCompound> compoundConsumer) {
        NbtCompound compound = new NbtCompound();
        compoundConsumer.accept(compound);
        return SetNbtLootFunction.builder(compound);
    }
}
