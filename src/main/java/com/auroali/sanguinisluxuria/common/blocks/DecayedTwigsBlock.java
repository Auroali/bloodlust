package com.auroali.sanguinisluxuria.common.blocks;

import net.minecraft.block.*;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class DecayedTwigsBlock extends WallMountedBlock implements Waterloggable {
    public static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(1, 1, 8, 15, 15, 16);
    public static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(1, 1, 0, 15, 15, 8);
    public static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0, 1, 1, 8, 15, 15);
    public static final VoxelShape WEST_SHAPE = Block.createCuboidShape(8, 1, 1, 16, 15, 15);
    public static final VoxelShape FLOOR_SHAPE = Block.createCuboidShape(1, 0, 1, 15, 8, 15);
    public static final VoxelShape CEILING_SHAPE = Block.createCuboidShape(1, 8, 1, 15, 16, 15);

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public DecayedTwigsBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
          .with(FACE, WallMountLocation.WALL)
          .with(FACING, Direction.NORTH)
          .with(WATERLOGGED, false)
        );
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        WallMountLocation mountLocation = state.get(FACE);
        if (mountLocation == WallMountLocation.CEILING)
            return CEILING_SHAPE;
        if (mountLocation == WallMountLocation.FLOOR)
            return FLOOR_SHAPE;

        return switch (WallMountedBlock.getDirection(state)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case DOWN -> CEILING_SHAPE;
            case UP -> FLOOR_SHAPE;
        };
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state != null)
            return state.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER));
        return null;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED))
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return type == NavigationType.AIR && !this.collidable || super.canPathfindThrough(state, world, pos, type);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
        builder.add(FACE);
        builder.add(WATERLOGGED);
    }
}
