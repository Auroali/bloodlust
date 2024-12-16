package com.auroali.sanguinisluxuria.common.blocks;

import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.auroali.sanguinisluxuria.common.registry.BLWorldgen;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class GraftedSaplingBlock extends SaplingBlock {
    public static final double BLOOD_DRAIN_RANGE = 8;

    public GraftedSaplingBlock(SaplingGenerator generator, Settings settings) {
        super(generator, settings);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return super.canGrow(world, random, pos, state) && this.isRightConditionsToGrow(world, pos);
    }

    @Override
    public void generate(ServerWorld world, BlockPos pos, BlockState state, Random random) {
        if (!this.isRightConditionsToGrow(world, pos))
            return;
        super.generate(world, pos, state, random);
    }

    public boolean isRightConditionsToGrow(World world, BlockPos pos) {
        if (world.isDay() && world.isSkyVisible(pos))
            return false;

        Box box = new Box(pos).expand(BLOOD_DRAIN_RANGE);
        AtomicBoolean hasDrainedBlood = new AtomicBoolean();
        world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), box, entity -> entity.getType().isIn(BLTags.Entities.HAS_BLOOD))
          .forEach(entity -> {
              BloodComponent bloodComponent = BLEntityComponents.BLOOD_COMPONENT.get(entity);
              if (bloodComponent.drainBlood()) {
                  world.playSound(null, pos, BLSounds.DRAIN_BLOOD, SoundCategory.BLOCKS);
                  hasDrainedBlood.set(true);
              }
          });
        return hasDrainedBlood.get();
    }

    public static class GraftedSaplingGenerator extends SaplingGenerator {
        @Nullable
        @Override
        protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
            return BLWorldgen.DECAYED_TREE;
        }
    }
}
