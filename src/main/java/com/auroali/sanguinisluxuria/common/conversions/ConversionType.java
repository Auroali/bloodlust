package com.auroali.sanguinisluxuria.common.conversions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public interface ConversionType {
    Entity apply(World world, Entity original, EntityType<?> targetType, NbtCompound tag);
}
