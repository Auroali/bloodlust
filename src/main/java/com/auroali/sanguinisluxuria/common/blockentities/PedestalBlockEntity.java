package com.auroali.sanguinisluxuria.common.blockentities;

import com.auroali.sanguinisluxuria.common.registry.BLBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity implements Clearable, ItemDisplayingBlockEntity {
    final SimpleInventory inv = new SimpleInventory(ItemStack.EMPTY);
    int spinTicks = 0;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(BLBlockEntities.PEDESTAL, pos, state);
        this.inv.addListener(inv -> {
            if (this.world != null && !this.world.isClient)
                this.world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        });
    }

    public static void tickClient(World world, BlockPos pos, BlockState state, PedestalBlockEntity entity) {
        entity.spinTicks++;
    }

    public ItemStack getItem() {
        return this.inv.getStack(0);
    }

    public void setItem(ItemStack stack) {
        this.inv.setStack(0, stack);
        if (this.world != null) {
            BlockState state = this.world.getBlockState(this.pos);
            this.world.updateListeners(this.pos, state, state, Block.NOTIFY_LISTENERS);
        }
        this.markDirty();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inv.setStack(0, ItemStack.fromNbt(nbt.getCompound("Item")));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("Item", this.inv.getStack(0).writeNbt(new NbtCompound()));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound data = new NbtCompound();
        data.put("Item", this.inv.getStack(0).writeNbt(new NbtCompound()));
        return data;
    }

    public Inventory getInventory() {
        return this.inv;
    }

    @Override
    public void clear() {
        this.inv.clear();
    }

    @Override
    public ItemStack getDisplayItem() {
        return this.inv.getStack(0);
    }

    @Override
    public int getDisplayTicks() {
        return this.spinTicks;
    }

    @Override
    public Vec3d getDisplayOffset() {
        return new Vec3d(0.5, 0.8, 0.5);
    }
}
