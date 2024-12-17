package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

import java.util.Map;

public class BLCauldronBehaviours {
    public static final Map<Item, CauldronBehavior> BLOOD_CAULDRON_BEHAVIOUR = CauldronBehavior.createMap();
    public static final CauldronBehavior BLOOD_STORING_ITEM_FILL = (state, world, pos, player, hand, stack) -> {
        if (stack.getItem() instanceof BloodStorageItem item) {
            if (BloodStorageItem.getStoredBlood(stack) >= BloodConstants.BLOOD_PER_BOTTLE) {
                int bloodToDrain = Math.min(BloodStorageItem.getStoredBlood(stack) / BloodConstants.BLOOD_PER_BOTTLE, LeveledCauldronBlock.MAX_LEVEL);
                BloodStorageItem.setStoredBlood(stack, BloodStorageItem.getStoredBlood(stack) - bloodToDrain * BloodConstants.BLOOD_PER_BOTTLE);
                if (item.getEmptyItem() != null && BloodStorageItem.getStoredBlood(stack) == 0) {
                    player.setStackInHand(hand, item.getEmptyItem().getDefaultStack());
                }
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
                world.setBlockState(pos, BLBlocks.BLOOD_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, bloodToDrain));
                return ActionResult.success(world.isClient);
            }
        }
        return ActionResult.FAIL;
    };
    public static final CauldronBehavior BLOOD_STORING_ITEM_DRAIN_FILL = (state, world, pos, player, hand, stack) -> {
        if (stack.getItem() instanceof BloodStorageItem item) {
            if (state.get(LeveledCauldronBlock.LEVEL) >= 1 && BloodStorageItem.getStoredBlood(stack) <= BloodStorageItem.getMaxBlood(stack) - BloodConstants.BLOOD_PER_BOTTLE) {
                BloodStorageItem.setStoredBlood(stack, BloodStorageItem.getStoredBlood(stack) + BloodConstants.BLOOD_PER_BOTTLE);

                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1, 1);
                return ActionResult.success(world.isClient);
            }

            int level = state.contains(LeveledCauldronBlock.LEVEL) ? state.get(LeveledCauldronBlock.LEVEL) : 0;
            int newLevel = Math.min(level + BloodStorageItem.getStoredBlood(stack) / BloodConstants.BLOOD_PER_BOTTLE, LeveledCauldronBlock.MAX_LEVEL);
            if (level == newLevel)
                return ActionResult.FAIL;

            BloodStorageItem.setStoredBlood(stack, BloodStorageItem.getStoredBlood(stack) - (newLevel - level) * BloodConstants.BLOOD_PER_BOTTLE);
            if (item.getEmptyItem() != null && BloodStorageItem.getStoredBlood(stack) == 0) {
                player.setStackInHand(hand, item.getEmptyItem().getDefaultStack());
            }
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            world.setBlockState(pos, BLBlocks.BLOOD_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, newLevel));
            return ActionResult.success(world.isClient);
        }
        return ActionResult.FAIL;
    };

    public static final CauldronBehavior FILL_GLASS_BOTTLE = (state, world, pos, player, hand, stack) -> {
        if (stack.isOf(Items.GLASS_BOTTLE)) {
            ItemStack bloodBottle = BloodStorageItem.setStoredBlood(new ItemStack(BLItems.BLOOD_BOTTLE), BloodConstants.BLOOD_PER_BOTTLE);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            stack.decrement(1);
            if (stack.isEmpty())
                player.setStackInHand(hand, bloodBottle);
            else if (!player.getInventory().insertStack(bloodBottle))
                player.dropItem(bloodBottle, false);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1, 1);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.FAIL;
    };

    public static void register() {
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(BLItems.BLOOD_BOTTLE, BLOOD_STORING_ITEM_FILL);
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(BLItems.BLOOD_BAG, BLOOD_STORING_ITEM_FILL);
        BLOOD_CAULDRON_BEHAVIOUR.put(BLItems.BLOOD_BOTTLE, BLOOD_STORING_ITEM_DRAIN_FILL);
        BLOOD_CAULDRON_BEHAVIOUR.put(BLItems.BLOOD_BAG, BLOOD_STORING_ITEM_DRAIN_FILL);
        BLOOD_CAULDRON_BEHAVIOUR.put(Items.GLASS_BOTTLE, FILL_GLASS_BOTTLE);
    }
}
