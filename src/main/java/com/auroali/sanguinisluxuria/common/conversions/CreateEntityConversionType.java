package com.auroali.sanguinisluxuria.common.conversions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public class CreateEntityConversionType implements ConversionType {
    @Override
    public Entity apply(World world, Entity original, EntityType<?> targetType, NbtCompound tag) {
        tag.putString("id", Registries.ENTITY_TYPE.getId(targetType).toString());

        return EntityType.getEntityFromNbt(tag, world)
          .map(newEntity -> {
              newEntity.setPosition(original.getX(), original.getY(), original.getZ());
              newEntity.setYaw(original.getYaw());
              newEntity.setPitch(original.getPitch());
              newEntity.setCustomName(original.getCustomName());
              return newEntity;
          })
          .orElse(null);
    }
}