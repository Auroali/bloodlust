package com.auroali.sanguinisluxuria.datagen.generators;

import com.auroali.sanguinisluxuria.datagen.builders.ConversionJsonBuilder;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class SanguinisLuxuriaConversionsProvider implements DataProvider {
    protected final FabricDataOutput output;
    protected final DataOutput.PathResolver pathResolver;

    protected SanguinisLuxuriaConversionsProvider(FabricDataOutput output) {
        this.output = output;
        this.pathResolver = this.output.getResolver(DataOutput.OutputType.DATA_PACK, "vampire_conversions");
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Set<ConversionJsonBuilder.Provider> providers = new HashSet<>();
        Set<Identifier> ids = new HashSet<>();
        this.generateConversions(providers::add);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (ConversionJsonBuilder.Provider provider : providers) {
            if (!ids.add(provider.getId()))
                throw new IllegalStateException("Duplicated id " + provider.getId());

            JsonObject json = new JsonObject();
            provider.serialize(json);
            ConditionJsonProvider.write(json, FabricDataGenHelper.consumeConditions(provider));
            futures.add(DataProvider.writeToPath(writer, json, this.getOutputPath(provider.getId())));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    protected Consumer<ConversionJsonBuilder.Provider> withConditions(Consumer<ConversionJsonBuilder.Provider> exporter, ConditionJsonProvider... conditions) {
        Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
        return provider -> {
            FabricDataGenHelper.addConditions(provider, conditions);
            exporter.accept(provider);
        };
    }

    protected abstract void generateConversions(Consumer<ConversionJsonBuilder.Provider> exporter);

    protected Path getOutputPath(Identifier id) {
        return this.pathResolver.resolve(id, "json");
    }

    @Override
    public String getName() {
        return "Vampire Conversions";
    }
}
