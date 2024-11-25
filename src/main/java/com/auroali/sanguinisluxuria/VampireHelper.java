package com.auroali.sanguinisluxuria;

import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLAdvancementCriterion;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.google.common.base.Predicates;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.function.Predicate;

public class VampireHelper {
    /**
     * Checks whether a living entity is a vampire
     *
     * @param entity the entity to check
     * @return whether the entity is a vampire
     */
    public static boolean isVampire(Entity entity) {
        return entity != null && BLEntityComponents.VAMPIRE_COMPONENT.isProvidedBy(entity) && BLEntityComponents.VAMPIRE_COMPONENT.get(entity).isVampire();
    }

    /**
     * Checks whether an entity is both a vampire and wearing a carved mask
     *
     * @param entity the entity to check
     * @return whether the entity is both a vampire and wearing a carved mask
     */
    public static boolean isMasked(LivingEntity entity) {
        return isVampire(entity)
          && TrinketsApi.getTrinketComponent(entity)
          .map(c -> c.isEquipped(i -> i.isIn(BLTags.Items.VAMPIRE_MASKS)))
          .orElse(false);
    }

    /**
     * Checks whether an entity can be converted to a vampire
     *
     * @param entity the potentially convertible entity
     * @return if the entity can be converted (has a vampire component and is not currently a vampire)
     */
    public static boolean canBeConvertedToVampire(LivingEntity entity) {
        return BLEntityComponents.VAMPIRE_COMPONENT.isProvidedBy(entity) && !BLEntityComponents.VAMPIRE_COMPONENT.get(entity).isVampire();
    }

    /**
     * Checks if an entity has an ability unlocked that is incompatible with the provided ability
     *
     * @param entity  the entity to check
     * @param ability the ability to check for incompatibilities with
     * @return if the entity has an incompatible ability unlocked
     */
    public static boolean hasIncompatibleAbility(LivingEntity entity, VampireAbility ability) {
        if (!isVampire(entity))
            return false;

        VampireComponent component = BLEntityComponents.VAMPIRE_COMPONENT.get(entity);

        return hasIncompatibleAbility(component.getAbilties(), ability);
    }

    /**
     * Checks if an ability container has an ability unlocked that is incompatible with the provided ability
     *
     * @param container the ability container to check
     * @param ability   the ability to check for incompatibilities with
     * @return if the ability container has an incompatible ability unlocked
     */
    public static boolean hasIncompatibleAbility(VampireAbilityContainer container, VampireAbility ability) {
        for (VampireAbility other : container) {
            if (ability.incompatibleWith(other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds all the toxic blood status effects to an entity
     *
     * @param entity the entity to add the effects to
     */
    public static void addToxicBloodEffects(LivingEntity entity) {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 3));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
        if (entity.getRandom().nextDouble() > 0.75)
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));
    }

    /**
     * Increments the level of blood sickness on an entity. If the entity does not currently have blood sickness, this will add it
     *
     * @param entity the entity to increment the blood sickness level of
     */
    public static void incrementBloodSickness(LivingEntity entity) {
        int level = entity.hasStatusEffect(BLStatusEffects.BLOOD_SICKNESS) ? entity.getStatusEffect(BLStatusEffects.BLOOD_SICKNESS).getAmplifier() + 1 : 0;
        entity.addStatusEffect(new StatusEffectInstance(BLStatusEffects.BLOOD_SICKNESS, 3600, level));
    }

    /**
     * Transfers status effects from one entity to the other, clearing the effects from the original entity
     *
     * @param from the entity to transfer effects from
     * @param to   the entity to transfer effects to
     */
    public static void transferStatusEffects(LivingEntity from, LivingEntity to) {
        for (StatusEffectInstance instance : from.getStatusEffects()) {
            if (instance.isAmbient())
                continue;

            to.addStatusEffect(instance);
        }

        if (from instanceof ServerPlayerEntity player) {
            BLAdvancementCriterion.TRANSFER_EFFECTS.trigger(player, player.getStatusEffects().size());
        }

        from.clearStatusEffects();
    }

    /**
     * Teleports an entity to a random position near their current one. Has the same behaviour as a chorus fruit
     *
     * @param entity the entity to teleport
     */
    public static void teleportRandomly(LivingEntity entity) {
        World world = entity.getWorld();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        for (int i = 0; i < 16; ++i) {
            double newPosX = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
            double newPosY = MathHelper.clamp(
              entity.getY() + (double) (entity.getRandom().nextInt(16) - 8),
              world.getBottomY(),
              (world.getBottomY() + ((ServerWorld) world).getLogicalHeight() - 1)
            );
            double newPosZ = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
            if (entity.hasVehicle()) {
                entity.stopRiding();
            }

            Vec3d pos = entity.getPos();
            if (entity.teleport(newPosX, newPosY, newPosZ, true)) {
                world.emitGameEvent(GameEvent.TELEPORT, pos, GameEvent.Emitter.of(entity));
                SoundEvent soundEvent = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                world.playSound(null, x, y, z, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                entity.playSound(soundEvent, 1.0F, 1.0F);
                break;
            }
        }
    }

    /**
     * Converts blood units to droplets
     *
     * @param blood the amount of blood
     * @return the amount of droplets
     */
    @SuppressWarnings("UnstableApiUsage")
    public static long bloodToDroplets(int blood) {
        return blood * FluidConstants.BOTTLE / BloodConstants.BLOOD_PER_BOTTLE;
    }

    /**
     * Converts droplets to blood units
     *
     * @param droplets the amount of droplets
     * @return the amount of blood, rounded down
     */
    @SuppressWarnings("UnstableApiUsage")
    public static int dropletsToBlood(long droplets) {
        return (int) (droplets * BloodConstants.BLOOD_PER_BOTTLE / FluidConstants.BOTTLE);
    }

    /**
     * Gets the item in an entity's hand
     *
     * @param entity        the entity to get the item from
     * @param initialHand   the hand to look in first
     * @param itemPredicate the predicate for the item
     * @return the item stack, or {@link net.minecraft.item.ItemStack#EMPTY} if nothing was found
     */
    public static ItemStack getItemInHand(LivingEntity entity, Hand initialHand, Predicate<ItemStack> itemPredicate) {
        // check the stack in the initialHand
        ItemStack stack = initialHand == Hand.MAIN_HAND ? entity.getMainHandStack() : entity.getOffHandStack();
        if (!stack.isEmpty() && itemPredicate.test(stack))
            return stack;

        // the first one didn't match, so check the other hand
        stack = initialHand == Hand.MAIN_HAND ? entity.getOffHandStack() : entity.getMainHandStack();
        if (!stack.isEmpty() && itemPredicate.test(stack))
            return stack;

        // no matches, must not be holding a valid item
        return ItemStack.EMPTY;
    }

    /**
     * Gets the item in an entity's hand
     *
     * @param entity      the entity to get the item from
     * @param initialHand the hand to look in first
     * @return the item stack, or {@link net.minecraft.item.ItemStack#EMPTY} if nothing was found
     * @see VampireHelper#getItemInHand(LivingEntity, Hand, Predicate)
     */
    public static ItemStack getItemInHand(LivingEntity entity, Hand initialHand) {
        return getItemInHand(entity, initialHand, Predicates.alwaysTrue());
    }

    public static Hand getHandForStack(LivingEntity entity, ItemStack stack) {
        return stack.isEmpty() || stack == entity.getMainHandStack() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }
}
