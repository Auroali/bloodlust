package com.auroali.sanguinisluxuria.datagen.generators;

import com.auroali.sanguinisluxuria.datagen.builders.BloodDrainEffectBuilder;
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

public abstract class SanguinisLuxuriaBloodEffectsProvider implements DataProvider {
    protected final FabricDataOutput output;
    protected final DataOutput.PathResolver pathResolver;

    protected SanguinisLuxuriaBloodEffectsProvider(FabricDataOutput output) {
        this.output = output;
        this.pathResolver = this.output.getResolver(DataOutput.OutputType.DATA_PACK, "blood_drain_effects");
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Set<BloodDrainEffectBuilder.Provider> providers = new HashSet<>();
        Set<Identifier> ids = new HashSet<>();
        this.generateEffects(providers::add);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (BloodDrainEffectBuilder.Provider provider : providers) {
            if (!ids.add(provider.getId()))
                throw new IllegalStateException("Duplicated id " + provider.getId());

            JsonObject json = new JsonObject();
            provider.serialize(json);
            ConditionJsonProvider.write(json, FabricDataGenHelper.consumeConditions(provider));
            futures.add(DataProvider.writeToPath(writer, json, this.getOutputPath(provider.getId())));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    protected Consumer<BloodDrainEffectBuilder.Provider> withConditions(Consumer<BloodDrainEffectBuilder.Provider> exporter, ConditionJsonProvider... conditions) {
        Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
        return provider -> {
            FabricDataGenHelper.addConditions(provider, conditions);
            exporter.accept(provider);
        };
    }

    protected abstract void generateEffects(Consumer<BloodDrainEffectBuilder.Provider> exporter);

    protected Path getOutputPath(Identifier id) {
        return this.pathResolver.resolve(id, "json");
    }

    @Override
    public String getName() {
        return "Blood Drain Effects";
    }
}
