package com.auroali.sanguinisluxuria.common.blocks;

import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;

public class HungryDecayedLogBlock extends PillarBlock {
    public static final IntProperty BLOOD_LEVEL = IntProperty.of("blood", 0, 3);

    public HungryDecayedLogBlock(Settings settings) {
        super(settings);
        this.setDefaultState(
          this.getStateManager().getDefaultState()
            .with(BLOOD_LEVEL, 0)
            .with(AXIS, Direction.Axis.Y)
        );
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        // fill glass bottles
        if (stack.isOf(Items.GLASS_BOTTLE) && state.get(BLOOD_LEVEL) >= 3) {
            stack.decrement(1);
            ItemStack bloodBottle = BloodStorageItem.setStoredBlood(new ItemStack(BLItems.BLOOD_BOTTLE), BloodConstants.BLOOD_PER_BOTTLE);
            if (stack.isEmpty())
                player.setStackInHand(hand, bloodBottle);
            else if (!player.getInventory().insertStack(bloodBottle))
                player.dropItem(bloodBottle, false, false);

            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.setBlockState(pos, state.with(BLOOD_LEVEL, 0));

            return ActionResult.success(world.isClient);
        }
        // fill blood storing items
        if (stack.getItem() instanceof BloodStorageItem bloodStoringItem && bloodStoringItem.canFill() && state.get(BLOOD_LEVEL) >= 3) {
            if (BloodStorageItem.getStoredBlood(stack) > BloodStorageItem.getMaxBlood(stack) - BloodConstants.BLOOD_PER_BOTTLE)
                return ActionResult.FAIL;

            BloodStorageItem.setStoredBlood(stack, BloodStorageItem.getStoredBlood(stack) + BloodConstants.BLOOD_PER_BOTTLE);

            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.setBlockState(pos, state.with(BLOOD_LEVEL, 0));

            return ActionResult.success(world.isClient);
        }
        return ActionResult.FAIL;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return (state.getBlock() == BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG || state.get(BLOOD_LEVEL) < 3) && super.hasRandomTicks(state);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(BLOOD_LEVEL) >= 3) {
            BlockPos lowerPosition = pos.down();
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockState lowerState = world.getBlockState(lowerPosition.offset(direction));
                BlockState neighbourState = world.getBlockState(pos.offset(direction));

                if (!neighbourState.isAir())
                    continue;

                if (lowerState.isOf(BLBlocks.BLOOD_CAULDRON) && lowerState.get(LeveledCauldronBlock.LEVEL) < 3) {
                    int newLevel = lowerState.get(LeveledCauldronBlock.LEVEL) + 1;
                    world.setBlockState(lowerPosition.offset(direction), lowerState.with(LeveledCauldronBlock.LEVEL, newLevel));
                }
                if (lowerState.isOf(Blocks.CAULDRON)) {
                    world.setBlockState(lowerPosition.offset(direction), BLBlocks.BLOOD_CAULDRON.getDefaultState());
                }
            }
            world.setBlockState(pos, state.with(BLOOD_LEVEL, 0));
            return;
        }
        if (random.nextInt(13) == 0) {
            int newLevel = state.get(BLOOD_LEVEL) + 1;
            Box boundingBox = new Box(pos).expand(5);

            List<LivingEntity> entities = world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), boundingBox, entity -> entity.getType().isIn(BLTags.Entities.HAS_BLOOD));

            for (LivingEntity entity : entities) {
                BloodComponent component = BLEntityComponents.BLOOD_COMPONENT.get(entity);
                if (component.drainBlood()) {
                    world.playSound(null, pos, BLSounds.DRAIN_BLOOD, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    world.setBlockState(pos, state.with(BLOOD_LEVEL, newLevel));
                    return;
                }
            }

        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BLOOD_LEVEL);
    }
}
