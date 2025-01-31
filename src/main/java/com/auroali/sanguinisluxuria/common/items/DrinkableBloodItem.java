package com.auroali.sanguinisluxuria.common.items;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.registry.BLBlocks;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrinkableBloodItem extends Item implements BloodStorageItem, EntityTrackingItem {
    public static final FoodComponent BLOOD_FOOD_COMPONENT = new FoodComponent.Builder()
            .hunger(1)
            .saturationModifier(0.05f)
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 360), 0.4f)
            .alwaysEdible()
            .build();

    private final int maxBlood;

    /**
     * Constructs a DrinkableBloodItem
     *
     * @param maxBlood the maximum amount of blood this item can hold (by default)
     * @param settings the item's settings. It's recommended you set the food component to the blood one
     * @see DrinkableBloodItem#BLOOD_FOOD_COMPONENT
     */
    public DrinkableBloodItem(int maxBlood, Settings settings) {
        super(settings.food(BLOOD_FOOD_COMPONENT));
        this.maxBlood = maxBlood;
    }

    @Override
    public int getDefaultMaxBlood() {
        return this.maxBlood;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        // copy the stack so that vanilla food logic can run
        // without decrementing the original stack size
        ItemStack stackCopy = stack.copy();
        if (!VampireHelper.hasBlood(user)) {
            user.eatFood(world, stackCopy);
            BloodStorageItem.decrementItemBlood(stack, 1);
            if (BloodStorageItem.getItemBlood(stack) == 0)
                return BloodStorageItem.createEmptyStackFor(stack);

            return stack;
        }

        BloodComponent userBlood = BLEntityComponents.BLOOD_COMPONENT.get(user);
        // calculate the blood to drain by taking the minimum of the
        // amount of additional blood the entity's blood component can store
        // and the amount of blood in the blood storage item. this value
        // is then clamped between 0 and 6, and added to the entity's blood
        // component and removed from the blood storage item's blood amount
        int bloodToFill = MathHelper.clamp(
                Math.min(userBlood.getMaxBlood() - userBlood.getBlood(), BloodStorageItem.getItemBlood(stack)),
                0,
                4
        );

        if (!(user instanceof PlayerEntity player && player.isCreative()))
            BloodStorageItem.decrementItemBlood(stack, bloodToFill);

        ItemStack result = BloodStorageItem.getItemBlood(stack) == 0 ? BloodStorageItem.createEmptyStackFor(stack) : stack;

        if (VampireHelper.isVampire(user) || user.hasStatusEffect(BLStatusEffects.BLOOD_LUST)) {
            // doing the same check twice probably isnt great
            // but it works
            // so thats something
            if (user.hasStatusEffect(BLStatusEffects.BLOOD_LUST))
                VampireHelper.incrementBloodSickness(user);
            // only add the blood to vampires
            userBlood.addBlood(bloodToFill);
            return result;
        }

        user.eatFood(world, stackCopy);
        VampireHelper.incrementBloodSickness(user);
        return result;
    }

    @Override
    public SoundEvent getDrinkSound() {
        return BLSounds.DRAIN_BLOOD;
    }

    @Override
    public SoundEvent getEatSound() {
        return BLSounds.DRAIN_BLOOD;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return BloodStorageItem.getItemBlood(stack) > 0 ? UseAction.DRINK : UseAction.NONE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (BloodStorageItem.getItemBlood(user.getStackInHand(hand)) <= 0)
            return TypedActionResult.pass(user.getStackInHand(hand));
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!BloodStorageItem.isItemDrainable(stack) || BloodStorageItem.getItemBlood(stack) < BloodConstants.BLOOD_PER_BOTTLE)
            return super.useOnBlock(context);

        ItemPlacementContext placementContext = new ItemPlacementContext(context);
        if (!placementContext.canPlace() || player == null || !player.isSneaking())
            return super.useOnBlock(context);

        BlockPos pos = placementContext.getBlockPos();
        BlockState bloodState = BLBlocks.BLOOD_SPLATTER.getPlacementState(placementContext);
        ShapeContext shapeContext = ShapeContext.of(player);
        // check to make sure the blockstate isn't null and that it can be placed at
        // the location. returns fail here if it can't be placed
        if (
                bloodState == null
                        || !bloodState.canPlaceAt(world, pos)
                        || !world.canPlace(bloodState, pos, shapeContext)
        ) {
            return ActionResult.FAIL;
        }

        // place the block
        world.setBlockState(pos, bloodState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);

        // if the player is in survival, drain blood from the bottle
        if (!player.isCreative()) {
            BloodStorageItem.decrementItemBlood(stack, BloodConstants.BLOOD_PER_BOTTLE);
            if (BloodStorageItem.getItemBlood(stack) <= 0)
                player.setStackInHand(context.getHand(), BloodStorageItem.createEmptyStackFor(stack));
        }
        // play the bottle empty sound and emit the block place game event
        world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.emitGameEvent(
                GameEvent.BLOCK_PLACE,
                pos,
                GameEvent.Emitter.of(player, bloodState)
        );

        return ActionResult.success(world.isClient);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        int blood = BloodStorageItem.getItemBlood(stack);
        return blood > 0 && blood < BloodStorageItem.getItemMaxBlood(stack);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.f * BloodStorageItem.getItemBlood(stack) / BloodStorageItem.getItemMaxBlood(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xFFDF0000;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        Text entityName = this.getEntityName(stack);
        if (entityName != null) {
            tooltip.add(entityName.copy().formatted(Formatting.DARK_RED));
        }
    }

    @Override
    public void setBlood(ItemStack stack, int blood) {
        if (this.getBlood(stack) > blood)
            EntityTrackingItem.clearEntity(stack);
        BloodStorageItem.super.setBlood(stack, blood);
    }
}
