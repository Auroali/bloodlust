package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface Ritual {
    Codec<Ritual> RITUAL_CODEC = BLRegistries.RITUAL_TYPES
      .getCodec()
      .dispatch("type", Ritual::getType, RitualType::getCodec);

    void onCompleted(World world, LivingEntity initiator, BlockPos pos, Inventory inventory);

    RitualType<?> getType();

    default void appendTooltips(List<Text> tooltips) {
    }
}
