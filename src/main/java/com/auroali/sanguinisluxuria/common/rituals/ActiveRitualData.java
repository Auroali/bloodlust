package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.Bloodlust;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.UUID;

public record ActiveRitualData(Ritual ritual, UUID initiator, UUID target) {
    public static final String RITUAL_KEY = "active_ritual";

    public static void writeNbt(NbtCompound compound, ActiveRitualData data) {
        if (data == null)
            return;
        NbtCompound ritualTag = new NbtCompound();
        Ritual.RITUAL_CODEC.encodeStart(NbtOps.INSTANCE, data.ritual())
          .resultOrPartial(Bloodlust.LOGGER::error)
          .ifPresent(element -> {
              ritualTag.putUuid("initiator", data.initiator);
              ritualTag.putUuid("target", data.target);
              ritualTag.put("ritual", element);
          });
    }

    public static ActiveRitualData readNbt(NbtCompound compound) {
        if (!compound.contains(RITUAL_KEY, NbtElement.COMPOUND_TYPE))
            return null;
        NbtCompound ritualTag = compound.getCompound(RITUAL_KEY);
        return Ritual.RITUAL_CODEC.parse(NbtOps.INSTANCE, ritualTag.get("ritual"))
          .resultOrPartial(Bloodlust.LOGGER::error)
          .map(ritual -> {
              UUID initiator = ritualTag.getUuid("initiator");
              UUID target = ritualTag.getUuid("target");
              return new ActiveRitualData(ritual, initiator, target);
          })
          .orElse(null);
    }

    public LivingEntity resolveTarget(World world) {
        if (this.target != null && world instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(this.target);
            if (entity instanceof LivingEntity living)
                return living;
        }
        return null;
    }

    public LivingEntity resolveInitiator(World world) {
        if (this.initiator != null && world instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(this.initiator);
            if (entity instanceof LivingEntity living)
                return living;
        }
        return null;
    }
}
