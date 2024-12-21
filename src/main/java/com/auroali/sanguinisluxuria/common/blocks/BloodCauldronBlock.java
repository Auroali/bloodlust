package com.auroali.sanguinisluxuria.common.blocks;

import com.auroali.sanguinisluxuria.common.registry.BLCauldronBehaviours;
import com.auroali.sanguinisluxuria.common.registry.BLFluids;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeTypes;
import com.google.common.base.Predicates;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Collections;

public class BloodCauldronBlock extends LeveledCauldronBlock {
    public BloodCauldronBlock(Settings settings) {
        super(settings, Predicates.alwaysFalse(), BLCauldronBehaviours.BLOOD_CAULDRON_BEHAVIOUR);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return Blocks.CAULDRON.getPickStack(world, pos, state);
    }

    @Override
    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return fluid == BLFluids.BLOOD;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        int level = state.get(LEVEL);
        if (world.isClient || entity.getType() != EntityType.ITEM)
            return;

        ItemEntity item = (ItemEntity) entity;
        ItemStack stack = item.getStack();

        world.getRecipeManager().getFirstMatch(BLRecipeTypes.BLOOD_CAULDRON_TYPE, new SimpleInventory(stack), world)
          .ifPresent(recipe -> {
              if (level < recipe.getCauldronLevel())
                  return;

              if (item.getOwner() instanceof ServerPlayerEntity player) {
                  Criteria.RECIPE_CRAFTED.trigger(player, recipe.getId(), Collections.singletonList(stack));
              }

              ItemStack result = recipe.craft(new SimpleInventory(stack), world.getRegistryManager());
              stack.decrement(1);
              if (item.getStack().isEmpty())
                  item.remove(Entity.RemovalReason.DISCARDED);

              ItemEntity newItem = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, result, world.random.nextGaussian() * 0.3, 0.47, world.random.nextGaussian() * 0.3);
              world.spawnEntity(newItem);
              world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);

              // drain the cauldron
              int newLevel = level - recipe.getCauldronLevel();
              BlockState blockState = newLevel == 0 ? Blocks.CAULDRON.getDefaultState() : state.with(LEVEL, newLevel);
              world.setBlockState(pos, blockState);
              world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
          });
    }
}
