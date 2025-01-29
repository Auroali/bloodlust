package com.auroali.sanguinisluxuria.common.entities;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.entities.goals.AcivateAbilityWhenDownedGoal;
import com.auroali.sanguinisluxuria.common.entities.goals.FleeWhenDownedGoal;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLVampireAbilities;
import com.auroali.sanguinisluxuria.common.registry.BLVampireVillagerTrades;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VampireMerchant extends MerchantEntity {
    long lastRestockTime;
    int restocksToday;
    private long lastRestockCheckTime;

    public VampireMerchant(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new AcivateAbilityWhenDownedGoal(this, BLVampireAbilities.MIST, true));
        this.goalSelector.add(1, new FleeWhenDownedGoal(this, 0.8d));
        this.goalSelector.add(1,
          new HoldInHandsGoal<>(
            this,
            PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.FIRE_RESISTANCE),
            SoundEvents.ENTITY_GENERIC_DRINK,
            merchant -> !merchant.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && merchant.getWorld().isDay() && merchant.getWorld().isSkyVisible(merchant.getBlockPos())
          ));
        this.goalSelector.add(2,
          new HoldInHandsGoal<>(
            this,
            BloodStorageItem.createStack(BLItems.BLOOD_BOTTLE),
            BLSounds.DRAIN_BLOOD,
            merchant -> VampireHelper.isVampire(merchant) && BLEntityComponents.VAMPIRE_COMPONENT.get(merchant).isDown()
          ));
        this.goalSelector.add(3, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.35f));
        this.goalSelector.add(8, new LookAtCustomerGoal(this));
        this.goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.f, 1.f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.f));
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        if (offer.shouldRewardPlayerExperience()) {
            int i = 3 + this.random.nextInt(4);
            this.getWorld().spawnEntity(new ExperienceOrbEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), i));
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                player.incrementStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.getOffers().isEmpty()) {
                if (!this.getWorld().isClient) {
                    this.setCustomer(player);
                    this.sendOffers(player, this.getDisplayName(), 1);
                }

            }
            return ActionResult.success(this.getWorld().isClient);
        } else {
            return super.interactMob(player, hand);
        }
    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    public void restock() {
//        for (TradeOffer tradeOffer : this.getOffers()) {
//            tradeOffer.resetUses();
//        }
        // regenerate the offer pool
        this.getOffers().clear();
        this.fillRecipes();
        this.lastRestockTime = this.getWorld().getTime();
        ++this.restocksToday;
    }

    private boolean needsRestock() {
        for (TradeOffer tradeOffer : this.getOffers()) {
            if (tradeOffer.hasBeenUsed()) {
                return true;
            }
        }

        return false;
    }

    private boolean canRestock() {
        return this.restocksToday == 0 || this.restocksToday < 2 && this.getWorld().getTime() > this.lastRestockTime + 2400L;
    }

    public boolean shouldRestock() {
        long nextRestockTime = this.lastRestockTime + 12000L;
        long currentTime = this.getWorld().getTime();
        boolean restockPossible = currentTime > nextRestockTime;
        long timeOfDay = this.getWorld().getTimeOfDay();
        if (this.lastRestockCheckTime > 0L) {
            long o = this.lastRestockCheckTime / 24000L;
            long p = timeOfDay / 24000L;
            restockPossible |= p > o;
        }

        this.lastRestockCheckTime = timeOfDay;
        if (restockPossible) {
            this.lastRestockTime = currentTime;
            this.restocksToday = 0;
        }

        return this.canRestock() && this.needsRestock();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.shouldRestock())
            this.restock();
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList offers = this.getOffers();
        if (offers != null) {
            this.fillRecipesFromPool(offers, BLVampireVillagerTrades.TRADES.get(1), 3);
            this.fillRecipesFromPool(offers, BLVampireVillagerTrades.TRADES.get(2), 1);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("RestocksToday", this.restocksToday);
        nbt.putLong("LastRestockTime", this.lastRestockTime);
        nbt.putLong("LastRestockCheckTime", this.lastRestockCheckTime);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.lastRestockTime = nbt.getLong("LastRestockTime");
        this.restocksToday = nbt.getInt("RestocksToday");
        this.lastRestockTime = nbt.getLong("LastRestockCheckTime");
    }
}
