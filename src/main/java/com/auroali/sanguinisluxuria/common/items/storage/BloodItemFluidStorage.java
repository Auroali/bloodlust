package com.auroali.sanguinisluxuria.common.items.storage;

import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.registry.BLFluids;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.minecraft.item.ItemStack;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class BloodItemFluidStorage implements Storage<FluidVariant>, StorageView<FluidVariant> {
    private final ContainerItemContext context;

    public BloodItemFluidStorage(ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack stack = this.context.getItemVariant().toStack();
        if (!resource.equals(this.getResource()) || maxAmount == 0)
            return 0;

        if (!BloodStorageItem.isItemFillable(stack) || BloodStorageItem.getItemBlood(stack) >= BloodStorageItem.getItemMaxBlood(stack))
            return 0;

        int bloodToFill = Math.min(
          BloodStorageItem.getItemMaxBlood(stack) - BloodStorageItem.getItemBlood(stack),
          BloodConstants.dropletsToBlood(maxAmount)
        );

        if (bloodToFill == 0)
            return 0;

        BloodStorageItem.incrementItemBlood(stack, bloodToFill);
        if (this.context.exchange(ItemVariant.of(stack), 1, transaction) == 1)
            return BloodConstants.bloodToDroplets(bloodToFill);

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack stack = this.context.getItemVariant().toStack();
        if (!resource.equals(this.getResource()) || maxAmount == 0)
            return 0;

        if (!BloodStorageItem.isItemDrainable(stack) || BloodStorageItem.getItemBlood(stack) == 0)
            return 0;

        int bloodToDrain = Math.min(
          BloodStorageItem.getItemBlood(stack),
          BloodConstants.dropletsToBlood(maxAmount)
        );

        BloodStorageItem.decrementItemBlood(stack, bloodToDrain);
        if (this.context.exchange(ItemVariant.of(stack), 1, transaction) == 1)
            return BloodConstants.bloodToDroplets(bloodToDrain);

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return BloodStorageItem.getItemBlood(this.context.getItemVariant().toStack()) == 0;
    }

    @Override
    public FluidVariant getResource() {
        return FluidVariant.of(BLFluids.BLOOD);
    }

    @Override
    public long getAmount() {
        ItemStack stack = this.context.getItemVariant().toStack();
        return BloodConstants.bloodToDroplets(BloodStorageItem.getItemBlood(stack));
    }

    @Override
    public long getCapacity() {
        ItemStack stack = this.context.getItemVariant().toStack();
        return BloodConstants.bloodToDroplets(BloodStorageItem.getItemMaxBlood(stack));
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return TransferApiImpl.singletonIterator(this);
    }
}
