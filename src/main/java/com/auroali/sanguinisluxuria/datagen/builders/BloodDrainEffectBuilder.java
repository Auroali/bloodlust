package com.auroali.sanguinisluxuria.datagen.builders;

import com.auroali.sanguinisluxuria.common.blood.BloodDrainEffectInstance;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BloodDrainEffectBuilder {
    final Either<TagKey<EntityType<?>>, EntityType<?>> target;
    final List<BloodDrainEffectInstance> effects;

    protected BloodDrainEffectBuilder(Either<TagKey<EntityType<?>>, EntityType<?>> target) {
        this.target = target;
        this.effects = new ArrayList<>();
    }

    public static BloodDrainEffectBuilder create(TagKey<EntityType<?>> tag) {
        return new BloodDrainEffectBuilder(Either.left(tag));
    }

    public static BloodDrainEffectBuilder create(EntityType<?> entity) {
        return new BloodDrainEffectBuilder(Either.right(entity));
    }

    public BloodDrainEffectBuilder effect(StatusEffect effect, int duration, int amplifier, float chance) {
        this.effects.add(new BloodDrainEffectInstance(effect, duration, amplifier, chance));
        return this;
    }

    public BloodDrainEffectBuilder effect(StatusEffect effect, int duration) {
        return this.effect(effect, duration, 0, 1.f);
    }

    public BloodDrainEffectBuilder effect(StatusEffect effect, float chance) {
        return this.effect(effect, 300, 0, chance);
    }

    public BloodDrainEffectBuilder effect(StatusEffect effect, int duration, float chance) {
        return this.effect(effect, duration, 0, chance);
    }

    public BloodDrainEffectBuilder effect(StatusEffect effect) {
        return this.effect(effect, 300, 0, 1.f);
    }

    protected void validate() {
        if (this.effects.isEmpty())
            throw new IllegalStateException("Effects must not be empty");
    }

    public void offerTo(Consumer<Provider> exporter, Identifier id) {
        this.validate();
        exporter.accept(new Provider(id, this.target, this.effects));
    }

    public static class Provider {
        final Identifier id;
        final Either<TagKey<EntityType<?>>, EntityType<?>> target;
        final List<BloodDrainEffectInstance> effects;

        protected Provider(Identifier id, Either<TagKey<EntityType<?>>, EntityType<?>> target, List<BloodDrainEffectInstance> effects) {
            this.id = id;
            this.target = target;
            this.effects = effects;
        }

        public void serialize(JsonObject object) {
            this.target
              .ifLeft(key -> object.addProperty("entity", "#" + key.id().toString()))
              .ifRight(entity -> object.addProperty("entity", Registries.ENTITY_TYPE.getId(entity).toString()));

            BloodDrainEffectInstance.CODEC.listOf()
              .encodeStart(JsonOps.INSTANCE, this.effects)
              .resultOrPartial(str -> {
                  throw new JsonParseException(str);
              })
              .ifPresent(element -> object.add("effects", element));
        }

        public Identifier getId() {
            return this.id;
        }
    }
}
