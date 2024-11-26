package com.auroali.sanguinisluxuria.common.entities;

import com.auroali.sanguinisluxuria.common.registry.BLVampireVillagerTrades;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
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
        for (TradeOffer tradeOffer : this.getOffers()) {
            tradeOffer.resetUses();
        }
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
        TradeOffers.Factory[] tradePool = BLVampireVillagerTrades.TRADES.get(1);
        if (offers != null) {
            this.fillRecipesFromPool(offers, tradePool, 6);
        }
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
