package com.auroali.sanguinisluxuria.common.items;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.registry.BLFluids;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class BloodStorageItem extends Item {
    final int maxBlood;
    Item emptyItem = null;

    public BloodStorageItem(Settings settings, int maxBlood) {
        super(settings);
        this.maxBlood = maxBlood;
    }

    public abstract boolean canFill();

    public abstract boolean canDrain();


    public Collection<ItemStack> generateGroupEntries() {
        List<ItemStack> stacks = new ArrayList<>();
        if (this.emptyItem == null)
            stacks.add(new ItemStack(this));
        stacks.add(setStoredBlood(new ItemStack(this), getMaxBlood()));
        return stacks;
    }

    public static boolean isHoldingBloodFillableItem(LivingEntity entity) {
        if (entity == null)
            return false;

        return (!getBloodStorageItemInHand(entity, Hand.OFF_HAND).isEmpty() && canBeFilled(getBloodStorageItemInHand(entity, Hand.OFF_HAND)))
          || (!getBloodStorageItemInHand(entity, Hand.MAIN_HAND).isEmpty() && canBeFilled(getBloodStorageItemInHand(entity, Hand.MAIN_HAND)));
    }

    /**
     * The item to replace this one with when it runs out of blood
     *
     * @param item the item to use
     * @see BLItems#BLOOD_BOTTLE
     */
    public BloodStorageItem emptyItem(Item item) {
        this.emptyItem = item;
        return this;
    }

    public static float getFillPercent(ItemStack stack) {
        return (float) getStoredBlood(stack) / getMaxBlood(stack);
    }

    /**
     * The model predicate to use with ModelPredicateProviderRegistry
     *
     * @see BloodStorageItem#registerModelPredicate()
     * @see net.minecraft.client.item.ModelPredicateProviderRegistry
     */
    @SuppressWarnings("unused")
    public static float modelPredicate(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
        return getFillPercent(stack);
    }

    /**
     * Registers a model predicate for fill percent with ModelPredicateProviderRegistery
     *
     * @apiNote The predicate's id is "sanguinisluxuria:blood_storage_item_fill"
     * @see net.minecraft.client.item.ModelPredicateProviderRegistry
     * @see BloodStorageItem#modelPredicate(ItemStack, ClientWorld, LivingEntity, int)
     */
    public static void registerModelPredicate(Item item) {
        ModelPredicateProviderRegistry.register(item, BLResources.BLOOD_STORAGE_ITEM_MODEL_PREDICATE, BloodStorageItem::modelPredicate);
    }

    /**
     * Sets the amount of blood stored in a given stack
     *
     * @param stack the blood storage item stack
     * @param blood the amount of blood
     */
    public static ItemStack setStoredBlood(ItemStack stack, int blood) {
        stack.getOrCreateNbt().putInt("StoredBlood", blood);
        return stack;
    }

    /**
     * Gets the maximum amount of blood this item can store
     *
     * @return the maximum amount of blood
     */
    public int getMaxBlood() {
        return maxBlood;
    }

    /**
     * Gets the maximum amount of blood this item can store
     *
     * @param stack the blood storing item
     * @return the maximum amount of blood
     */
    public static int getMaxBlood(ItemStack stack) {
        if (stack.getItem() instanceof BloodStorageItem item)
            return item.getMaxBlood();
        return 0;
    }

    /**
     * Gets the amount of blood stored in a stack
     *
     * @param stack the item stack
     * @return the amount of blood stored in the stack
     */
    public static int getStoredBlood(ItemStack stack) {
        return stack.getOrCreateNbt().getInt("StoredBlood");
    }

    public static boolean canBeFilled(ItemStack stack) {
        return stack.getItem() instanceof BloodStorageItem item && item.canFill();
    }

    public static boolean canBeDrained(ItemStack stack) {
        return stack.getItem() instanceof BloodStorageItem item && item.canDrain();
    }

    private static ItemStack getBloodStorageItemInHand(LivingEntity entity, Hand hand) {
        ItemStack stack = entity.getStackInHand(hand);
        if (stack.isOf(Items.GLASS_BOTTLE))
            return new ItemStack(BLItems.BLOOD_BOTTLE);
        return stack.getItem() instanceof BloodStorageItem ? stack : ItemStack.EMPTY;
    }

    /**
     * Tries to fill a valid blood-storing item in an entity's hand
     *
     * @param entity the entity holding the item
     * @param amount the amount of blood to add
     * @return if the entity was both holding a valid item and the item could successfully be filled by amount
     */
    public static boolean tryAddBloodToItemInHand(LivingEntity entity, int amount) {
        // get the currently held blood storage item
        // will prefer to use the main hand
        ItemStack stack = getBloodStorageItemInHand(entity, Hand.OFF_HAND);
        Hand hand = Hand.OFF_HAND;
        if (!getBloodStorageItemInHand(entity, Hand.MAIN_HAND).isEmpty()) {
            stack = getBloodStorageItemInHand(entity, Hand.MAIN_HAND);
            hand = Hand.MAIN_HAND;
        }

        // if no item was found, or it cannot be filled, return false
        if (stack.isEmpty() || !canBeFilled(stack) || getStoredBlood(stack) + amount > getMaxBlood(stack))
            return false;

        setStoredBlood(stack, getStoredBlood(stack) + amount);

        Item originalHeldItem = entity.getStackInHand(hand).getItem();

        // decrement the held stack if it isn't the same as the final stack
        // this occurs in situations like filling bottles
        if (stack != entity.getStackInHand(hand))
            entity.getStackInHand(hand).decrement(1);

        // set the item in the player's hand, or drop it if there isn't enough inventory space
        if (stack == entity.getStackInHand(hand) || entity.getStackInHand(hand).isEmpty())
            entity.setStackInHand(hand, stack);
        else if (!(entity instanceof PlayerEntity e && e.getInventory().insertStack(stack))) {
            if (entity instanceof PlayerEntity player)
                player.dropItem(stack, false);
            else entity.dropStack(stack);
        }

        // put the item on cooldown
        if (entity instanceof PlayerEntity player)
            player.getItemCooldownManager().set(originalHeldItem, 10);

        return true;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        // if theres no blood or the item is full, we don't need to display the fill bar
        return getStoredBlood(stack) > 0 && getStoredBlood(stack) != getMaxBlood(stack);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.f * getFillPercent(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xFFDF0000;
    }

    /**
     * @return the item to replace this one with when emptied
     */
    public Item getEmptyItem() {
        return this.emptyItem;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class FluidStorage implements Storage<FluidVariant>, StorageView<FluidVariant> {
        private final ContainerItemContext context;
        private final BloodStorageItem item;
        private final FluidVariant containedFluid;

        public FluidStorage(ContainerItemContext ctx, BloodStorageItem bloodStoringItem) {
            this.context = ctx;
            this.item = bloodStoringItem;
            this.containedFluid = FluidVariant.of(BLFluids.BLOOD);
        }

        private long getStoredFluid() {
            if (context.getItemVariant().getNbt() == null)
                return 0;
            return VampireHelper.bloodToDroplets(context.getItemVariant().getNbt().getInt("StoredBlood"));
        }

        private long getMaxStoredFluid() {
            return VampireHelper.bloodToDroplets(item.getMaxBlood());
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            Item emptyItem = item.getEmptyItem() == null ? item : item.emptyItem;
            long insertableAmount = Math.min(maxAmount, getMaxStoredFluid() - getStoredFluid());

            // Can't insert if the item is not emptyItem anymore.
            if (!context.getItemVariant().isOf(emptyItem) || !item.canFill()) return 0;

            // Make sure that the fluid and amount match.
            if (resource.isOf(BLFluids.BLOOD) && insertableAmount != 0) {
                // If that's ok, just convert one of the empty item into the full item, with the mapping function.
                ItemVariant newVariant = ItemVariant.of(setStoredBlood(new ItemStack(this.item), VampireHelper.dropletsToBlood(getStoredFluid() + insertableAmount)));

                if (context.exchange(newVariant, 1, transaction) == 1) {
                    // Conversion ok!
                    return insertableAmount;
                }
            }

            return 0;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            // If the context's item is not fullItem anymore, can't extract!
            if (!context.getItemVariant().isOf(item) || !item.canDrain()) return 0;

            long storedAmount = Math.min(getStoredFluid(), maxAmount);
            // Make sure that the fluid and the amount match.
            if (resource.equals(containedFluid) && storedAmount != 0) {
                // If that's ok, just convert one of the full item into the empty item, copying the nbt.
                ItemVariant newVariant = getStoredFluid() - storedAmount > 0
                  ? ItemVariant.of(setStoredBlood(new ItemStack(item), VampireHelper.dropletsToBlood(getStoredFluid() - storedAmount)))
                  : this.item.getEmptyItem() == null ? ItemVariant.of(this.item) : ItemVariant.of(this.item.getEmptyItem());

                if (context.exchange(newVariant, 1, transaction) == 1) {
                    // Conversion ok!
                    return storedAmount;
                }
            }

            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            ItemVariant variant = context.getItemVariant();
            return !variant.isOf(item) || (!variant.hasNbt() || variant.getNbt().getInt("StoredBlood") == 0);
        }

        @Override
        public FluidVariant getResource() {
            return containedFluid;
        }

        @Override
        public long getAmount() {
            return getStoredFluid();
        }

        @Override
        public long getCapacity() {
            return getMaxStoredFluid();
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            return TransferApiImpl.singletonIterator(this);
        }
    }
}
