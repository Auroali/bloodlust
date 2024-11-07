package com.auroali.sanguinisluxuria.common.items;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class DrinkableBloodStorageItem extends BloodStorageItem {
    public DrinkableBloodStorageItem(Settings settings, int maxBlood) {
        super(settings, maxBlood);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        if (!world.isClient) {
            BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(user);
            int bloodToAdd = Math.min(blood.getMaxBlood() - blood.getBlood(), getStoredBlood(stack));
            if (bloodToAdd == 0)
                return stack;

            if (!VampireHelper.isVampire(user)) {
                applyNonVampireEffects(user);
                // i know i could use a food component but this seems like it gives more control
                if (user instanceof ServerPlayerEntity e && e.getRandom().nextInt(2) == 0)
                    e.getHungerManager().add(1, 0);
            } else if (VampireHelper.isVampire(user))
                bloodToAdd = BLEntityComponents.BLOOD_COMPONENT.get(user).addBlood(bloodToAdd);

            if (!(user instanceof PlayerEntity entity && entity.isCreative()))
                setStoredBlood(stack, getStoredBlood(stack) - bloodToAdd);
            if (getStoredBlood(stack) == 0 && emptyItem != null)
                return new ItemStack(emptyItem, stack.getCount());
        }


        return stack;
    }

    public static void applyNonVampireEffects(LivingEntity user) {
        if (user.hasStatusEffect(BLStatusEffects.BLOOD_PROTECTION) || user.getWorld().isClient)
            return;

        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 40, 0));
        if (user.getRandom().nextInt(2) == 0)
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 120, 0));
        if (user.getRandom().nextInt(12) == 0)
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));

        VampireHelper.incrementBloodSickness(user);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (getStoredBlood(user.getStackInHand(hand)) == 0)
            return TypedActionResult.pass(user.getStackInHand(hand));
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 40;
    }

    @Override
    public SoundEvent getDrinkSound() {
        return BLSounds.DRAIN_BLOOD;
    }

    @Override
    public boolean canFill() {
        return true;
    }

    @Override
    public boolean canDrain() {
        return true;
    }
}
