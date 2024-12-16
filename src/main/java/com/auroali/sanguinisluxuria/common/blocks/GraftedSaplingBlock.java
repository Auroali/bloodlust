package com.auroali.sanguinisluxuria.common.blocks;

import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import net.minecraft.block.*;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.ArrayList;
import java.util.List;

public class GraftedSaplingBlock extends PlantBlock implements Fertilizable {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    public static final IntProperty STAGE = Properties.STAGE;

    public GraftedSaplingBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(STAGE, 0));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public void tryGrow(World world, BlockState state, BlockPos pos, Random random) {
        if (state.get(STAGE) == 0) {
            world.setBlockState(pos, state.cycle(STAGE));
        }
        int height = random.nextBetween(3, 4);
        int hungryLogPos = random.nextBetween(1, height - 2);
        List<BlockPos> placedBlocks = new ArrayList<>(15);
        boolean generateHungryLog = random.nextInt(3) == 0;
        // place the main logs
        for (int i = 0; i < height; i++) {
            BlockState logBlock = i != hungryLogPos || !generateHungryLog ? BLBlocks.DECAYED_LOG.getDefaultState() : BLBlocks.HUNGRY_DECAYED_LOG.getDefaultState();
            BlockPos logPos = pos.up(i);
            ItemPlacementContext placementContext = new AutomaticItemPlacementContext(world, logPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP);
            BlockState existingState = world.getBlockState(logPos);
            if (!existingState.isOf(BLBlocks.GRAFTED_SAPLING) && !existingState.canReplace(placementContext))
                break;
            world.setBlockState(logPos, logBlock, Block.NOTIFY_ALL);
            if (!logBlock.isOf(BLBlocks.HUNGRY_DECAYED_LOG))
                placedBlocks.add(logPos);
        }

        // place supporting logs
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (random.nextInt(3) != 0)
                continue;

            int lowerBranchHeight = random.nextBetween(2, 3);
            int offset = random.nextInt(2);
            for (int i = offset; i < lowerBranchHeight; i++) {
                BlockState logBlock = i == offset ? BLBlocks.DECAYED_WOOD.getDefaultState() : BLBlocks.DECAYED_LOG.getDefaultState();
                BlockPos logPos = pos.offset(direction).down(i);
                ItemPlacementContext placementContext = new AutomaticItemPlacementContext(world, logPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP);
                if (!world.getBlockState(logPos).canReplace(placementContext))
                    break;
                world.setBlockState(logPos, logBlock, Block.NOTIFY_ALL);
                placedBlocks.add(logPos);
            }
        }

        // post process all placed log blocks
        // adds in the twigs
        BlockState decayedTwigsState = BLBlocks.DECAYED_TWIGS.getDefaultState();
        for (BlockPos logPos : placedBlocks) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                if (random.nextInt(6) != 0 || !world.getBlockState(logPos.offset(direction)).isAir())
                    continue;
                world.setBlockState(logPos.offset(direction), decayedTwigsState.with(Properties.HORIZONTAL_FACING, direction).with(Properties.WALL_MOUNT_LOCATION, WallMountLocation.WALL), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.isNight() && world.isSkyVisible(pos) && random.nextInt(7) == 0) {
            this.tryGrow(world, state, pos, random);
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return world.isNight() && world.isSkyVisible(pos) && random.nextFloat() < 0.25;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.tryGrow(world, state, pos, random);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}
