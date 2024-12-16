package com.auroali.sanguinisluxuria.common.worldgen;


import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class DecayedTwigsDecorator extends TreeDecorator {
    public static final Codec<DecayedTwigsDecorator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Codec.FLOAT.fieldOf("chance").forGetter(DecayedTwigsDecorator::getChance)
    ).apply(instance, DecayedTwigsDecorator::new));

    final float chance;

    public DecayedTwigsDecorator(float chance) {
        this.chance = chance;
    }

    public float getChance() {
        return this.chance;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return BLWorldgen.DECAYED_TWIGS_DECORATOR;
    }

    @Override
    public void generate(Generator generator) {
        BlockState state = BLBlocks.DECAYED_TWIGS.getDefaultState();
        generator.getLogPositions().forEach(blockPos -> {
            for (Direction direction : Direction.values()) {
                BlockPos pos = blockPos.offset(direction);
                // if the position is not air or the roll fails, skip
                if (!generator.isAir(pos) || generator.getRandom().nextFloat() > this.chance) {
                    continue;
                }

                if (direction.getAxis() == Direction.Axis.Y) {
                    generator.replace(
                      pos,
                      state
                        .with(Properties.WALL_MOUNT_LOCATION, direction == Direction.UP ? WallMountLocation.FLOOR : WallMountLocation.CEILING)
                        .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                    );
                }

                if (direction.getAxis() == Direction.Axis.X || direction.getAxis() == Direction.Axis.Z) {
                    generator.replace(
                      pos,
                      state
                        .with(Properties.WALL_MOUNT_LOCATION, WallMountLocation.WALL)
                        .with(Properties.HORIZONTAL_FACING, direction)
                    );
                }
            }
        });
    }
}
