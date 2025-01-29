package com.auroali.sanguinisluxuria.common.rituals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public record RitualParameters(World world, BlockPos pos, Inventory inventory, LivingEntity initiator,
                               LivingEntity target) {
    public void applyToPlayerInitiator(Consumer<ServerPlayerEntity> consumer) {
        if (this.initiator instanceof ServerPlayerEntity player)
            consumer.accept(player);
    }

    public void applyToPlayerTarget(Consumer<ServerPlayerEntity> consumer) {
        if (this.target instanceof ServerPlayerEntity player)
            consumer.accept(player);
    }

    public static RitualParametersBuilder builder() {
        return new RitualParametersBuilder();
    }

    public static class RitualParametersBuilder {
        private World world;
        private BlockPos pos;
        private Inventory inventory;
        private LivingEntity initiator;
        private LivingEntity target;

        protected RitualParametersBuilder() {
        }

        public RitualParametersBuilder position(BlockPos pos) {
            this.pos = pos;
            return this;
        }

        public RitualParametersBuilder world(World world) {
            this.world = world;
            return this;
        }

        public RitualParametersBuilder inventory(Inventory inventory) {
            this.inventory = inventory;
            return this;
        }

        public RitualParametersBuilder initiator(LivingEntity entity) {
            this.initiator = entity;
            return this;
        }

        public RitualParametersBuilder target(LivingEntity entity) {
            this.target = entity;
            return this;
        }

        public RitualParameters build() {
            return new RitualParameters(this.world, this.pos, this.inventory, this.initiator, this.target != null ? this.target : this.initiator);
        }
    }
}
