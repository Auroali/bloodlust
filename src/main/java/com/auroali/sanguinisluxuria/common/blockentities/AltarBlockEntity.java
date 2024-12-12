package com.auroali.sanguinisluxuria.common.blockentities;

import com.auroali.sanguinisluxuria.BloodlustClient;
import com.auroali.sanguinisluxuria.common.blocks.AltarBlock;
import com.auroali.sanguinisluxuria.common.particles.DelayedParticleEffect;
import com.auroali.sanguinisluxuria.common.registry.BLBlockEntities;
import com.auroali.sanguinisluxuria.common.registry.BLParticles;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AltarBlockEntity extends BlockEntity implements Inventory, ItemDisplayingBlockEntity {
    DefaultedList<ItemStack> recipeItems = DefaultedList.ofSize(8, ItemStack.EMPTY);
    ItemStack storedItem = ItemStack.EMPTY;
    int ticks;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(BLBlockEntities.ALTAR, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.storedItem = ItemStack.fromNbt(nbt.getCompound("StoredItem"));
        this.recipeItems = DefaultedList.ofSize(8, ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.recipeItems);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("StoredItem", this.storedItem.writeNbt(new NbtCompound()));
        Inventories.writeNbt(nbt, this.recipeItems);
    }

    public static void tickClient(World world, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        altar.ticks++;
        if (!state.get(AltarBlock.ACTIVE))
            return;

        BloodlustClient.isAltarActive = true;
        if (altar.ticks % 20 == 0) {
            // todo: this crashes on the dedicated server
            world.playSound(MinecraftClient.getInstance().player, pos, BLSounds.ALTAR_BEATS, SoundCategory.BLOCKS);
            world.addParticle(new DelayedParticleEffect(BLParticles.ALTAR_BEAT, 2), pos.getX() + 0.5, pos.getY() + 0.05f, pos.getZ() + 0.5, 0, 0, 0);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, AltarBlockEntity altar) {

    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.storedItem.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot == 0 ? this.storedItem : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return slot == 0 ? this.storedItem.split(amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot != 0)
            return ItemStack.EMPTY;
        ItemStack item = this.storedItem;
        this.storedItem = ItemStack.EMPTY;
        return item;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0)
            this.storedItem = stack;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.storedItem = ItemStack.EMPTY;
    }


    @Override
    public ItemStack getDisplayItem() {
        return this.storedItem;
    }

    @Override
    public int getDisplayTicks() {
        return this.ticks;
    }

    @Override
    public Vec3d getDisplayOffset() {
        return new Vec3d(0.5, 0.45, 0.5);
    }
}
