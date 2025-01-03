package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.conversions.*;
import com.auroali.sanguinisluxuria.common.conversions.transformers.CopyConversionTransformer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BLConversions implements IdentifiableResourceReloadListener {
    private static final HashMap<EntityType<?>, List<EntityConversionData>> CONVERSIONS = new HashMap<>();
    private static final ResourceFinder FINDER = new ResourceFinder("vampire_conversions", ".json");
    private static final Gson GSON = new Gson();

    public static final ConversionType SET_VAMPIRE_TYPE = new VampireSettingConversionType(true);
    public static final ConversionType REVERT_VAMPIRE_TYPE = new VampireSettingConversionType(true);
    public static final ConversionType SPAWN_TYPE = new CreateEntityConversionType();
    public static final EntityConversionTransformer.Serializer<?> COPY = new EntityConversionTransformer.Serializer<>(CopyConversionTransformer::fromJson);

    public static void register() {
        Registry.register(BLRegistries.CONVERSION_TYPES, BLResources.SET_VAMPIRE_TYPE, SET_VAMPIRE_TYPE);
        Registry.register(BLRegistries.CONVERSION_TYPES, BLResources.REVERT_VAMPIRE_TYPE, REVERT_VAMPIRE_TYPE);
        Registry.register(BLRegistries.CONVERSION_TYPES, BLResources.SPAWN_TYPE, SPAWN_TYPE);
        Registry.register(BLRegistries.CONVERSION_TRANSFORMERS, BLResources.COPY_ID, COPY);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
          .registerReloadListener(new BLConversions());
    }

    public static void convertEntity(Entity entity) {
        List<EntityConversionData> conversions = CONVERSIONS.get(entity.getType());
        for (EntityConversionData conversion : conversions) {
            if (!conversion.testConditions(entity))
                continue;

            conversion.performConversion(entity.getWorld(), entity);
            return;
        }
    }

    @Override
    public Identifier getFabricId() {
        return BLResources.CONVERSION_DATA;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
              List<EntityConversionData> data = new ArrayList<>();
              FINDER.findResources(manager).forEach((identifier, resource) -> {
                  try {
                      JsonElement element = GSON.fromJson(resource.getReader(), JsonElement.class);
                      EntityConversionData conversionData = EntityConversionData.fromJson(element.getAsJsonObject());
                      data.add(conversionData);
                  } catch (IOException | JsonParseException e) {
                      Bloodlust.LOGGER.error("Failed to read conversion", e);
                  }
              });
              return data;
          }, prepareExecutor)
          .thenCompose(synchronizer::whenPrepared)
          .thenAcceptAsync(conversions -> {
              CONVERSIONS.clear();
              for (EntityConversionData data : conversions) {
                  CONVERSIONS.computeIfAbsent(data.getEntity(), key -> new ArrayList<>())
                    .add(data);
              }
          }, applyExecutor);
    }
}
