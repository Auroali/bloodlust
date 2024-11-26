package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.common.registry.BLEntities;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class BLEntityTagsProvider extends FabricTagProvider<EntityType<?>> {
    public BLEntityTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ENTITY_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.getOrCreateTagBuilder(BLTags.Entities.HAS_BLOOD)
          .add(EntityType.SHEEP)
          .add(EntityType.COW)
          .add(EntityType.PLAYER)
          .add(EntityType.FOX)
          .add(EntityType.WOLF)
          .add(EntityType.CAT)
          .add(EntityType.OCELOT)
          .add(EntityType.DONKEY)
          .add(EntityType.HORSE)
          .add(EntityType.MULE)
          .add(EntityType.VILLAGER)
          .add(EntityType.EVOKER)
          .add(EntityType.PILLAGER)
          .add(EntityType.VINDICATOR)
          .add(EntityType.ILLUSIONER)
          .add(EntityType.WITCH)
          .add(EntityType.WANDERING_TRADER)
          .add(EntityType.LLAMA)
          .add(EntityType.PANDA)
          .add(EntityType.PIG)
          .add(EntityType.PIGLIN)
          .add(EntityType.PIGLIN_BRUTE)
          .add(EntityType.HOGLIN)
          .add(EntityType.GOAT)
          .add(EntityType.POLAR_BEAR)
          .add(EntityType.RAVAGER)
          .add(EntityType.ZOMBIE)
          .add(EntityType.ZOMBIE_VILLAGER)
          .add(EntityType.ZOMBIE_HORSE)
          .add(EntityType.HUSK)
          .add(EntityType.DROWNED)
          .add(EntityType.ZOGLIN)
          .add(EntityType.ZOMBIFIED_PIGLIN)
          .add(EntityType.ENDERMAN)
          .add(EntityType.TRADER_LLAMA)
          .add(BLEntities.VAMPIRE_VILLAGER)
          .add(BLEntities.VAMPIRE_MERCHANT)
          .addOptional(new Identifier("ratsmischief", "rat"))
          .addOptional(new Identifier("spectrum", "egg_laying_wooly_pig"))
          .addOptional(new Identifier("spectrum", "kindling"));

        this.getOrCreateTagBuilder(BLTags.Entities.GOOD_BLOOD)
          .add(EntityType.VILLAGER)
          .add(EntityType.PILLAGER)
          .add(EntityType.VINDICATOR)
          .add(EntityType.EVOKER)
          .add(EntityType.WITCH)
          .add(EntityType.WANDERING_TRADER)
          .add(EntityType.PLAYER);

        this.getOrCreateTagBuilder(BLTags.Entities.TOXIC_BLOOD)
          .add(EntityType.ZOMBIE)
          .add(EntityType.ZOMBIE_VILLAGER)
          .add(EntityType.ZOMBIE_HORSE)
          .add(EntityType.HUSK)
          .add(EntityType.DROWNED)
          .add(EntityType.ZOGLIN)
          .add(EntityType.ZOMBIFIED_PIGLIN);

        this.getOrCreateTagBuilder(BLTags.Entities.CAN_DROP_BLOOD)
          .add(EntityType.VILLAGER)
          .add(EntityType.EVOKER)
          .add(EntityType.VINDICATOR)
          .add(EntityType.PILLAGER);

        this.getOrCreateTagBuilder(BLTags.Entities.TELEPORTS_ON_DRAIN)
          .add(EntityType.ENDERMAN);
    }
}
