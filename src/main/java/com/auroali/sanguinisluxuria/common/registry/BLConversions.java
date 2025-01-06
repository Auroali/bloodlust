package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.conversions.*;
import com.auroali.sanguinisluxuria.common.conversions.conditions.ConversionContextCondition;
import com.auroali.sanguinisluxuria.common.conversions.conditions.OrConversionCondition;
import com.auroali.sanguinisluxuria.common.conversions.transformers.CopyConversionTransformer;
import com.auroali.sanguinisluxuria.common.events.VampireConversionEvents;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
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
    public static final ConversionType REVERT_VAMPIRE_TYPE = new VampireSettingConversionType(false);
    public static final ConversionType SPAWN_TYPE = new CreateEntityConversionType();

    public static final EntityConversionTransformer.Serializer<?> COPY_TRANSFORMER = new EntityConversionTransformer.Serializer<>(CopyConversionTransformer::fromJson);

    public static final EntityConversionCondition.Serializer<?> CONVERSION_CONTEXT_CONDITION = new EntityConversionCondition.Serializer<>(ConversionContextCondition::fromJson);
    public static final EntityConversionCondition.Serializer<?> OR_CONDITION = new EntityConversionCondition.Serializer<>(OrConversionCondition::fromJson);

    public static void register() {
        Registry.register(BLRegistries.CONVERSION_TYPES, BLResources.SET_VAMPIRE_TYPE, SET_VAMPIRE_TYPE);
        Registry.register(BLRegistries.CONVERSION_TYPES, BLResources.REVERT_VAMPIRE_TYPE, REVERT_VAMPIRE_TYPE);
        Registry.register(BLRegistries.CONVERSION_TYPES, BLResources.SPAWN_TYPE, SPAWN_TYPE);
        Registry.register(BLRegistries.CONVERSION_TRANSFORMERS, BLResources.COPY_TRANSFORMER_ID, COPY_TRANSFORMER);
        Registry.register(BLRegistries.CONVERSION_CONDITIONS, BLResources.CONVERSION_CONTEXT_CONDITION_ID, CONVERSION_CONTEXT_CONDITION);
        Registry.register(BLRegistries.CONVERSION_CONDITIONS, BLResources.OR_CONDITION_ID, OR_CONDITION);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
          .registerReloadListener(new BLConversions());
    }

    public static boolean convertEntity(ConversionContext context) {
        List<EntityConversionData> conversions = CONVERSIONS.get(context.entity().getType());
        if (conversions == null || conversions.isEmpty())
            return false;

        if (!VampireConversionEvents.ALLOW_CONVERSION.invoker().allowConversion(context))
            return false;

        for (EntityConversionData conversion : conversions) {
            if (!conversion.testConditions(context))
                continue;

            conversion.performConversion(context);
            return true;
        }
        return false;
    }

    @Override
    public Identifier getFabricId() {
        return BLResources.CONVERSION_DATA;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> FINDER.findResources(manager), prepareExecutor)
          .thenApply(resources -> {
              List<EntityConversionData> data = new ArrayList<>();
              resources.forEach((identifier, resource) -> {
                  try {
                      JsonObject object = GSON.fromJson(resource.getReader(), JsonObject.class);
                      if (object.has(ResourceConditions.CONDITIONS_KEY) && !ResourceConditions.objectMatchesConditions(object))
                          return;

                      EntityConversionData conversionData = EntityConversionData.fromJson(object);
                      data.add(conversionData);
                  } catch (IOException | JsonParseException e) {
                      Bloodlust.LOGGER.error("Failed to read conversion", e);
                  }
              });
              return data;
          })
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
