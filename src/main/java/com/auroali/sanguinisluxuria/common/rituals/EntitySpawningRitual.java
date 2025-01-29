package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;

public record EntitySpawningRitual(EntityType<?> type, NbtCompound nbt, boolean autoTame) implements Ritual {
    public static final Codec<EntitySpawningRitual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Registries.ENTITY_TYPE.getCodec().fieldOf("id").forGetter(EntitySpawningRitual::type),
      NbtCompound.CODEC.optionalFieldOf("nbt", null).forGetter(EntitySpawningRitual::nbt),
      Codec.BOOL.optionalFieldOf("autoTame", false).forGetter(EntitySpawningRitual::autoTame)
    ).apply(instance, EntitySpawningRitual::new));

    public static EntitySpawningRitual create(EntityType<?> entity) {
        return new EntitySpawningRitual(entity, null, false);
    }

    public static EntitySpawningRitual create(EntityType<?> entity, NbtCompound compound) {
        return new EntitySpawningRitual(entity, compound, false);
    }

    public static EntitySpawningRitual create(EntityType<?> entity, boolean autoTame) {
        return new EntitySpawningRitual(entity, null, autoTame);
    }


    @Override
    public void onCompleted(RitualParameters parameters) {
        NbtCompound compound = this.nbt == null ? new NbtCompound() : this.nbt().copy();
        compound.putString("id", EntityType.getId(this.type).toString());
        EntityType.getEntityFromNbt(compound, parameters.world())
          .ifPresent(entity -> {
              Vec3d position = parameters.pos().toCenterPos().add(0, 1, 0);
              entity.setPosition(position);
              parameters.world().spawnEntity(entity);
              if (this.autoTame() && entity instanceof TameableEntity tameable)
                  parameters.applyToPlayerInitiator(tameable::setOwner);
          });
    }

    @Override
    public RitualType<?> getType() {
        return BLRitualTypes.ENTITY_SPAWNING_RITUAL_TYPE;
    }
}
