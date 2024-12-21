package com.auroali.sanguinisluxuria.common.components;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.VampireHungerManager;
import com.auroali.sanguinisluxuria.common.abilities.InfectiousAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.events.BloodEvents;
import com.auroali.sanguinisluxuria.common.registry.*;
import com.auroali.sanguinisluxuria.config.BLConfig;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.event.GameEvent;

public interface VampireComponent extends Component, AutoSyncedComponent, ServerTickingComponent {
    /**
     * @return if the component holder is a vampire
     */
    boolean isVampire();

    /**
     * Sets whether the component holder is a vampire
     *
     * @param isVampire whether the holder is a vampire
     */
    void setIsVampire(boolean isVampire);

    /**
     * Drains blood from a target entity, filling up the component holder's blood
     * or damaging them if the target has blood protection
     *
     * @param entity the entity to drain from
     * @see BloodComponent#drainBlood(LivingEntity)
     */
    void drainBloodFrom(LivingEntity entity);

    /**
     * Attempts to find a valid target to drain blood from, and start draining blood
     * if one is found. If a valid target cannot be found, it will instead attempt to
     * fill any blood storage items in the component holder's hands
     */
    void tryStartSuckingBlood();

    /**
     * Cancels any blood draining
     */
    void stopSuckingBlood();

    /**
     * Gets the current blood draining progress
     *
     * @return the amount of time blood has been draining for, in ticks
     * @see BloodConstants#BLOOD_DRAIN_TIME
     * @see BloodConstants#BLOOD_DRAIN_TIME_BLEEDING
     */
    int getBloodDrainTimer();

    /**
     * Gets the max time that can be spent in the sun before burning
     *
     * @return the amount of time that can be spent in the sun, in ticks
     */
    int getMaxTimeInSun();

    /**
     * Gets the amount of time spent in the sun
     *
     * @return how long the component holder has been in the sun, in ticks
     */
    int getTimeInSun();

    /**
     * Gets the ability container for this component, which tracks unlocked abilities,
     * ability cooldowns and what abilities are bound to keys
     *
     * @return this component's ability container
     */
    VampireAbilityContainer getAbilties();

    void unlockAbility(VampireAbility ability);

    /**
     * Gets the downed state of this component
     *
     * @return the downed state
     */
    boolean isDown();

    /**
     * Sets the 'downed' state of this component
     *
     * @param down whether downed or not
     */
    void setDowned(boolean down);

    boolean isMist();

    void setMist(boolean isMist);

    /**
     * Calculates the amount of damage taken by a vampire for a given damage source
     *
     * @param amount the initial amount of damage
     * @param source the damage source
     * @return the amount of damage that should be taken by the vampire
     * @see BLConfig#vampireDamageMultiplier
     */
    static float calculateDamage(float amount, DamageSource source) {
        if (source.isIn(BLTags.DamageTypes.VAMPIRES_WEAK_TO))
            return amount * BLConfig.INSTANCE.vampireDamageMultiplier;

        return amount;
    }

    /**
     * Checks if a particular damage source is effective against vampires
     *
     * @param source the damage source
     * @return whether the source is effective and damage should be increased
     */
    static boolean isEffectiveAgainstVampires(DamageSource source) {
        if (source.isIn(BLTags.DamageTypes.VAMPIRES_WEAK_TO))
            return true;

        if (source.getAttacker() instanceof LivingEntity entity && entity.getAttributeValue(BLEntityAttributes.BLESSED_DAMAGE) > 0) {
            return true;
        }

        if (source.getAttacker() instanceof LivingEntity entity) {
            ItemStack stack = entity.getMainHandStack();
            int level = EnchantmentHelper.getLevel(Enchantments.SMITE, stack);
            return level > 0;
        }

        return false;
    }

    static void handleBloodDrain(VampireComponent vampire, LivingEntity target, LivingEntity vampireEntity) {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(target);
        // if the target doesn't have blood or cannot be drained, we can't fill hunger
        if (!blood.hasBlood() || !BloodEvents.ALLOW_BLOOD_DRAIN.invoker().allowBloodDrain(vampireEntity, target) || !blood.drainBlood(vampireEntity))
            return;

        // damage the vampire and cancel filling up hunger if the target has blood protection
        if (target.hasStatusEffect(BLStatusEffects.BLOOD_PROTECTION)) {
            vampireEntity.damage(BLDamageSources.blessedWater(target), BLConfig.INSTANCE.blessedWaterDamage);
            return;
        }

        // handle differing amounts of blood depending on the good blood tag and unlocked abilities
        int bloodAmount = 1;

        if (!VampireHelper.isVampire(target) && target.getType().isIn(BLTags.Entities.GOOD_BLOOD))
            bloodAmount *= 2;

        // handle differences between adding blood to the player and regular entities
        // (such as saturation)
        if (vampireEntity instanceof PlayerEntity player)
            ((VampireHungerManager) player.getHungerManager()).sanguinisluxuria$addHunger(bloodAmount, 0.125f);
        else BLEntityComponents.BLOOD_COMPONENT.get(vampireEntity).addBlood(bloodAmount);

        BloodEvents.BLOOD_DRAINED.invoker().onBloodDrained(vampireEntity, target, bloodAmount);


        // reset the downed state
        vampire.setDowned(false);

        vampireEntity.getWorld().emitGameEvent(vampireEntity, GameEvent.DRINK, vampireEntity.getPos());

        // if the potion transfer ability is unlocked, transfer potion effects to the target
        if (vampire.getAbilties().hasAbility(BLVampireAbilities.INFECTIOUS)) {
            BLVampireAbilities.INFECTIOUS.sync(target, InfectiousAbility.InfectiousData.create(target, vampireEntity.getStatusEffects()));
            VampireHelper.transferStatusEffects(vampireEntity, target);
        }

        BLEntityBloodDrainEffects.applyTo(vampireEntity, target);

        // allow conversion of entities with weakness
        if (!VampireHelper.isVampire(target) && target.hasStatusEffect(StatusEffects.WEAKNESS)) {
            if (vampireEntity instanceof ServerPlayerEntity player)
                BLAdvancementCriterion.INFECT_ENTITY.trigger(player);
            VampireHelper.incrementBloodSickness(target);
        }

        // villagers have a 50% chance to wake up when having their blood drained
        // it also adds negative reputation to the player
        if (target.getWorld() instanceof ServerWorld serverWorld && target instanceof VillagerEntity villager) {
            serverWorld.handleInteraction(EntityInteraction.VILLAGER_HURT, vampireEntity, villager);
        }

        if (vampireEntity.getRandom().nextDouble() > 0.5f)
            target.wakeUp();

        if (target.getType().isIn(BLTags.Entities.TELEPORTS_ON_DRAIN)) {
            VampireHelper.teleportRandomly(vampireEntity);
        }
    }
}
