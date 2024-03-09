package com.auroali.sanguinisluxuria.common.blocks;

import com.auroali.sanguinisluxuria.common.blockentities.SkillUpgraderBlockEntity;
import com.auroali.sanguinisluxuria.common.registry.BLBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class SkillUpgraderBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(0, 0, 0, 16, 7, 16),
            Block.createCuboidShape(1, 7, 1, 15, 14, 15),
            Block.createCuboidShape(3, 7, 3, 13, 16, 13)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    public SkillUpgraderBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getStateManager().getDefaultState().with(ACTIVE, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        SkillUpgraderBlockEntity altar = world.getBlockEntity(pos) instanceof SkillUpgraderBlockEntity e ? e : null;
        if(altar == null)
            return ActionResult.FAIL;

        altar.checkAndStartRecipe(world, player);

        return ActionResult.success(world.isClient);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SkillUpgraderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if(world.isClient)
            return null;
        return checkType(type, BLBlockEntities.SKILL_UPGRADER, SkillUpgraderBlockEntity::tick);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ACTIVE);
    }
}
