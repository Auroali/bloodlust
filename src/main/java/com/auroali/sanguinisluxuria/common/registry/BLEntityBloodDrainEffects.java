package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.blood.BloodDrainEffectInstance;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BLEntityBloodDrainEffects implements IdentifiableResourceReloadListener {
    private static final HashMap<EntityType<?>, List<BloodDrainEffectInstance>> EFFECT_MAP = new HashMap<>();
    private static final List<LoadedEffects> UNRESOLVED_EFFECTS = new ArrayList<>();
    private static final Gson GSON = new Gson();
    private static final ResourceFinder FINDER = new ResourceFinder("blood_drain_effects", "json");

    public static List<BloodDrainEffectInstance> getFor(EntityType<?> type) {
        return EFFECT_MAP.get(type);
    }

    public static void applyTo(LivingEntity drainer, LivingEntity entity) {
        List<BloodDrainEffectInstance> effects = getFor(entity.getType());
        if (effects == null)
            return;

        effects.forEach(effect -> {
            if (drainer.getRandom().nextFloat() > effect.chance())
                return;
            drainer.addStatusEffect(new StatusEffectInstance(effect.effect(), effect.duration(), effect.amplifier()));
        });
    }

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
          .registerReloadListener(new BLEntityBloodDrainEffects());
    }

    public static void resolveReferences() {
        EFFECT_MAP.clear();
        for (LoadedEffects entry : UNRESOLVED_EFFECTS) {
            List<EntityType<?>> targets = entry.resolveTargets();
            List<BloodDrainEffectInstance> effects = entry.effects();
            targets.forEach(entity ->
              mergeEffects(effects, EFFECT_MAP.computeIfAbsent(entity, key -> new ArrayList<>()))
            );
        }
        UNRESOLVED_EFFECTS.clear();
    }

    @Override
    public Identifier getFabricId() {
        return BLResources.BLOOD_DRAIN_EFFECTS;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        // this is terrible
        // awful code
        // but at least it works!
        // someday i will make it not terrible
        return CompletableFuture.supplyAsync(() -> FINDER.findResources(manager), prepareExecutor)
          // read effect files
          .thenApply(resources -> {
              List<LoadedEffects> entries = new ArrayList<>();
              resources.forEach((id, resource) -> {
                  try {
                      JsonObject object = GSON.fromJson(resource.getReader(), JsonObject.class);
                      if (object.has(ResourceConditions.CONDITIONS_KEY) && !ResourceConditions.objectMatchesConditions(object))
                          return;

                      entries.add(LoadedEffects.fromJson(object));
                  } catch (JsonParseException | IOException e) {
                      Bloodlust.LOGGER.error("Could not parse entity blood drain effect {}", id, e);
                  }
              });
              return entries;
          })
          // wait for apply stage
          .thenCompose(synchronizer::whenPrepared)
          // tags aren't loaded yet, so store the loaded effects into a cache
          .thenAcceptAsync(UNRESOLVED_EFFECTS::addAll, applyExecutor);
    }

    private static void mergeEffects(List<BloodDrainEffectInstance> from, List<BloodDrainEffectInstance> to) {
        for (BloodDrainEffectInstance effect : from) {
            boolean hasMerged = false;
            for (int i = 0; i < to.size(); i++) {
                BloodDrainEffectInstance existing = to.get(i);
                if (effect.effect() != existing.effect())
                    continue;
                to.set(i, BloodDrainEffectInstance.merge(effect, existing));
                hasMerged = true;
            }
            if (hasMerged)
                continue;

            to.add(effect);
        }
    }

    /**
     * Intermediate representation of a list of blood drain effects
     * <br> Required because tags aren't loaded until after effects are, so the tag references can't be resolved until after
     * datapack reload
     *
     * @param targets either a direct entity type reference, or a tag
     * @param effects the list of effects
     */
    private record LoadedEffects(Either<TagKey<EntityType<?>>, EntityType<?>> targets,
                                 List<BloodDrainEffectInstance> effects) {
        List<EntityType<?>> resolveTargets() {
            return this.targets().map(
              tag -> BLTags.getAllEntriesInTag(tag, Registries.ENTITY_TYPE),
              Collections::singletonList
            );
        }

        public static LoadedEffects fromJson(JsonObject object) {
            // handle effects
            List<BloodDrainEffectInstance> effects = new ArrayList<>();
            for (JsonElement element : object.getAsJsonArray("effects")) {
                if (!element.isJsonObject())
                    throw new JsonParseException("Expected json object but got " + element);

                BloodDrainEffectInstance.CODEC.parse(JsonOps.INSTANCE, element)
                  .resultOrPartial(Bloodlust.LOGGER::error)
                  .ifPresent(effects::add);
            }

            Either<TagKey<EntityType<?>>, EntityType<?>> targets;

            // handle parsing target
            String targetString = object.get("entity").getAsString();
            if (targetString.startsWith("#")) {
                Identifier id = Identifier.tryParse(targetString.substring(1));
                if (id == null)
                    throw new JsonParseException("Failed to parse id " + targetString + " for tag");
                TagKey<EntityType<?>> tag = TagKey.of(RegistryKeys.ENTITY_TYPE, id);
                targets = Either.left(tag);
            } else {
                Identifier id = Identifier.tryParse(targetString);
                if (id == null)
                    throw new JsonParseException("Failed to parse id " + targetString + " for entity");
                EntityType<?> type = Registries.ENTITY_TYPE.get(id);
                targets = Either.right(type);
            }

            return new LoadedEffects(targets, effects);
        }
    }
}
