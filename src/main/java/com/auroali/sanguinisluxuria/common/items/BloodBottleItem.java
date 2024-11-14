package com.auroali.sanguinisluxuria.common.items;

import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.event.GameEvent;

public class BloodBottleItem extends DrinkableBloodStorageItem {
    public BloodBottleItem(Settings settings, int maxBlood) {
        super(settings, maxBlood);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemPlacementContext placementContext = new ItemPlacementContext(context);
        // if the item can't be drained or doesn't have enough blood, return
        if (!BloodStorageItem.canBeDrained(context.getStack()) || BloodStorageItem.getStoredBlood(context.getStack()) < BloodConstants.BLOOD_PER_BOTTLE)
            return super.useOnBlock(context);

        if (placementContext.canPlace() && context.getPlayer() != null && context.getPlayer().isSneaking()) {
            BlockState bloodState = BLBlocks.BLOOD_SPLATTER.getPlacementState(placementContext);
            ShapeContext shapeContext = ShapeContext.of(context.getPlayer());
            // check to make sure the blockstate isn't null and that it can be placed at
            // the location. returns fail here if it can't be placed
            if (
              bloodState == null
                || !bloodState.canPlaceAt(placementContext.getWorld(), placementContext.getBlockPos())
                || !placementContext.getWorld().canPlace(bloodState, placementContext.getBlockPos(), shapeContext)
            ) {
                return ActionResult.FAIL;
            }

            // place the block
            context.getWorld().setBlockState(placementContext.getBlockPos(), bloodState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);

            // if the player is in survival, drain blood from the bottle
            if (!context.getPlayer().isCreative()) {
                BloodStorageItem.setStoredBlood(context.getStack(), getStoredBlood(context.getStack()) - BloodConstants.BLOOD_PER_BOTTLE);
                if (this.emptyItem != null && BloodStorageItem.getStoredBlood(context.getStack()) <= 0)
                    context.getPlayer().setStackInHand(context.getHand(), new ItemStack(this.emptyItem));
            }
            // play the bottle empty sound and emit the block place game event
            context.getWorld().playSound(context.getPlayer(), placementContext.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            context.getWorld().emitGameEvent(
              GameEvent.BLOCK_PLACE,
              placementContext.getBlockPos(),
              GameEvent.Emitter.of(context.getPlayer(), bloodState)
                                            );

            return ActionResult.success(context.getWorld().isClient);
        }
        return super.useOnBlock(context);
    }
}
