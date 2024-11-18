package com.auroali.sanguinisluxuria.common.blocks;

import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BloodSplatterBlock extends Block {
    private static final VoxelShape DOT_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
    private static final Map<Direction, VoxelShape> DIRECTION_TO_SIDE_SHAPE = Maps.newEnumMap(
      ImmutableMap.of(
        Direction.NORTH,
        Block.createCuboidShape(3.0, 0.0, 0.0, 13.0, 1.0, 13.0),
        Direction.SOUTH,
        Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 1.0, 16.0),
        Direction.EAST,
        Block.createCuboidShape(3.0, 0.0, 3.0, 16.0, 1.0, 13.0),
        Direction.WEST,
        Block.createCuboidShape(0.0, 0.0, 3.0, 13.0, 1.0, 13.0)
      )
    );
    private static final Map<Direction, VoxelShape> DIRECTION_TO_UP_SHAPE = Maps.newEnumMap(
      ImmutableMap.of(
        Direction.NORTH,
        VoxelShapes.union(DIRECTION_TO_SIDE_SHAPE.get(Direction.NORTH), Block.createCuboidShape(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)),
        Direction.SOUTH,
        VoxelShapes.union(DIRECTION_TO_SIDE_SHAPE.get(Direction.SOUTH), Block.createCuboidShape(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)),
        Direction.EAST,
        VoxelShapes.union(DIRECTION_TO_SIDE_SHAPE.get(Direction.EAST), Block.createCuboidShape(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)),
        Direction.WEST,
        VoxelShapes.union(DIRECTION_TO_SIDE_SHAPE.get(Direction.WEST), Block.createCuboidShape(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))
      )
    );
    private static final HashMap<BlockState, VoxelShape> SHAPES = new HashMap<>();

    private static final BooleanProperty PERSISTENT = Properties.PERSISTENT;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_NORTH = Properties.NORTH_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_EAST = Properties.EAST_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_SOUTH = Properties.SOUTH_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_WEST = Properties.WEST_WIRE_CONNECTION;
    public static final Map<Direction, EnumProperty<WireConnection>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY = Maps.newEnumMap(
      ImmutableMap.of(
        Direction.NORTH, WIRE_CONNECTION_NORTH, Direction.EAST, WIRE_CONNECTION_EAST, Direction.SOUTH, WIRE_CONNECTION_SOUTH, Direction.WEST, WIRE_CONNECTION_WEST
      )
    );

    public BloodSplatterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
          .with(PERSISTENT, false)
          .with(WIRE_CONNECTION_NORTH, WireConnection.NONE)
          .with(WIRE_CONNECTION_EAST, WireConnection.NONE)
          .with(WIRE_CONNECTION_SOUTH, WireConnection.NONE)
          .with(WIRE_CONNECTION_WEST, WireConnection.NONE)
        );

        for (BlockState state : this.getStateManager().getStates()) {
            if (state.get(PERSISTENT))
                continue;

            SHAPES.put(state, this.getShapeForState(state));
        }
    }

    public VoxelShape getShapeForState(BlockState state) {
        VoxelShape shape = DOT_SHAPE;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            WireConnection connection = state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
            if (connection == WireConnection.SIDE)
                shape = VoxelShapes.union(shape, DIRECTION_TO_SIDE_SHAPE.get(direction));
            if (connection == WireConnection.UP)
                shape = VoxelShapes.union(shape, DIRECTION_TO_UP_SHAPE.get(direction));
        }
        return shape;
    }

    public BlockState getConnectionForDirection(WorldAccess world, BlockState state, BlockPos pos, Direction direction) {
        BlockState otherState = world.getBlockState(pos);
        if (connectsToBlock(otherState) || connectsToBlock(world.getBlockState(pos.down())))
            return state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), WireConnection.SIDE);
        if (!otherState.isSideSolidFullSquare(world, pos, direction.getOpposite()))
            return state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), WireConnection.NONE);
        otherState = world.getBlockState(pos.up());
        if (connectsToBlock(otherState))
            return state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), WireConnection.UP);
        return state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), WireConnection.NONE);
    }

    public boolean connectsToBlock(BlockState otherState) {
        return otherState.isOf(this) || otherState.isOf(BLBlocks.ALTAR) || otherState.isOf(BLBlocks.PEDESTAL);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(Items.GLASS_BOTTLE)) {
            ItemStack bloodBottle = new ItemStack(BLItems.BLOOD_BOTTLE);
            BloodStorageItem.setStoredBlood(bloodBottle, BLItems.BLOOD_BOTTLE.getMaxBlood());
            if (!world.isClient) {
                stack.decrement(1);
                if (!player.getInventory().insertStack(bloodBottle))
                    player.dropItem(bloodBottle, true);
                world.removeBlock(pos, false);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }

            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (!state.canPlaceAt(world, pos))
            world.removeBlock(pos, false);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockState newState = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        if (direction == Direction.UP || direction == Direction.DOWN)
            return newState;

        if (neighborPos.equals(pos.offset(direction)) || neighborPos.equals(pos.up().offset(direction)) || neighborPos.equals(pos.down().offset(direction))) {
            newState = getConnectionForDirection(world, newState, neighborPos, direction);
        }
        return newState;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (oldState.isOf(state.getBlock()) || world.isClient)
            return;

        state.updateNeighbors(world, pos.up(), Block.NOTIFY_ALL);
        state.updateNeighbors(world, pos.down(), Block.NOTIFY_ALL);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (state.isOf(newState.getBlock()) || world.isClient)
            return;

        state.updateNeighbors(world, pos.up(), Block.NOTIFY_ALL);
        state.updateNeighbors(world, pos.down(), Block.NOTIFY_ALL);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.with(PERSISTENT, false));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (!state.get(PERSISTENT) && random.nextInt(25) == 0)
            world.removeBlock(pos, false);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = this.getDefaultState().with(PERSISTENT, true);
        return getConnectionState(ctx.getWorld(), state, ctx.getBlockPos());
    }

    public BlockState getConnectionState(WorldAccess world, BlockState state, BlockPos pos) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            state = getConnectionForDirection(world, state, pos.offset(direction), direction);
        }
        return state;
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return context.getStack().isEmpty() || !context.getStack().isOf(this.asItem());
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return BloodStorageItem.setStoredBlood(new ItemStack(BLItems.BLOOD_BOTTLE), BloodConstants.BLOOD_PER_BOTTLE);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(PERSISTENT);
        builder.add(WIRE_CONNECTION_NORTH);
        builder.add(WIRE_CONNECTION_EAST);
        builder.add(WIRE_CONNECTION_SOUTH);
        builder.add(WIRE_CONNECTION_WEST);
    }
}
