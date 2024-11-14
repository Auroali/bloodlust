package com.auroali.sanguinisluxuria.common.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class PredicatePressurePlateBlock extends AbstractPressurePlateBlock {
    public static final BooleanProperty POWERED = Properties.POWERED;
    private final PressurePlateBlock.ActivationRule type;
    Predicate<? super Entity> entityPredicate;

    public PredicatePressurePlateBlock(PressurePlateBlock.ActivationRule type, Predicate<? super Entity> entityPredicate, Settings settings, BlockSetType blockSetType) {
        super(settings, blockSetType);
        this.entityPredicate = entityPredicate;
        this.type = type;
        this.setDefaultState(this.getStateManager().getDefaultState().with(POWERED, false));
    }

    @Override
    protected int getRedstoneOutput(BlockState state) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    protected BlockState setRedstoneOutput(BlockState state, int rsOut) {
        return state.with(POWERED, rsOut > 0);
    }

    @Override
    protected int getRedstoneOutput(World world, BlockPos pos) {
        Class<? extends Entity> entityClass = switch (this.type) {
            case EVERYTHING -> Entity.class;
            case MOBS -> LivingEntity.class;
        };
        return getEntityCount(world, BOX.offset(pos), entityClass, entityPredicate) > 0 ? 15 : 0;
    }

    protected static int getEntityCount(World world, Box box, Class<? extends Entity> entityClass, Predicate<? super Entity> predicate) {
        return world.getEntitiesByClass(
          entityClass,
          box,
          EntityPredicates.EXCEPT_SPECTATOR.and(entity -> !entity.canAvoidTraps()).and(predicate)
                                       ).size();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
