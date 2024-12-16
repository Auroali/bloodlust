package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public record StatusEffectRitual(List<StatusEffect> effects, int duration, Target target) implements Ritual {
    public static final Codec<StatusEffectRitual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Codec.either(Registries.STATUS_EFFECT.getCodec(), Registries.STATUS_EFFECT.getCodec().listOf())
        .xmap(either -> either.map(List::of, list -> list), Either::right)
        .fieldOf("effect").forGetter(StatusEffectRitual::effects),
      Codec.INT.optionalFieldOf("duration", 3600).forGetter(StatusEffectRitual::duration),
      Target.CODEC.optionalFieldOf("target", Target.INITIATOR).forGetter(StatusEffectRitual::target)
    ).apply(instance, StatusEffectRitual::new));

    @Override
    public void onCompleted(World world, LivingEntity initiator, BlockPos pos, Inventory inventory) {
        switch (this.target()) {
            case ALL -> {
                this.applyToEntity(initiator, initiator);
                this.applyToOthers(world, initiator, pos);
            }
            case OTHER -> this.applyToOthers(world, initiator, pos);
            case INITIATOR -> this.applyToEntity(initiator, initiator);
        }
    }

    public void applyToEntity(LivingEntity entity, LivingEntity initiator) {
        this.effects().forEach(effect ->
          entity.addStatusEffect(new StatusEffectInstance(effect, this.duration(), 0), initiator)
        );
    }

    public void applyToOthers(World world, LivingEntity initiator, BlockPos pos) {
        world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), new Box(pos).expand(16.d), entity -> entity != initiator && entity.isAlive())
          .forEach(entity ->
            this.applyToEntity(entity, initiator)
          );
    }

    @Override
    public RitualType<?> getType() {
        return BLRitualTypes.STATUS_EFFECT_RITUAL_TYPE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public List<StatusEffect> effects;
        public int duration;
        public Target target;

        protected Builder() {
            this.effects = new ArrayList<>();
            this.duration = 3600;
            this.target = Target.INITIATOR;
        }

        public Builder addEffect(StatusEffect effect) {
            this.effects.add(effect);
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder target(Target target) {
            this.target = target;
            return this;
        }

        public StatusEffectRitual build() {
            return new StatusEffectRitual(this.effects, this.duration, this.target);
        }
    }

    public enum Target implements StringIdentifiable {
        INITIATOR("self"),
        OTHER("others"),
        ALL("all");

        public static final com.mojang.serialization.Codec<Target> CODEC = StringIdentifiable
          .createCodec(Target::values);

        private final String name;

        Target(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
