package com.auroali.sanguinisluxuria.common.blockentities;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.BloodlustClient;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.blocks.AltarBlock;
import com.auroali.sanguinisluxuria.common.network.AltarRecipeStartS2C;
import com.auroali.sanguinisluxuria.common.network.SpawnAltarBeatParticleS2C;
import com.auroali.sanguinisluxuria.common.registry.BLAdvancementCriterion;
import com.auroali.sanguinisluxuria.common.registry.BLBlockEntities;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeTypes;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.rituals.Ritual;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AltarBlockEntity extends BlockEntity implements Inventory, ItemDisplayingBlockEntity {
    public static final int INVENTORY_SIZE = 1;
    public static final int PEDESTAL_SEARCH_RADIUS = 8;
    DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    int ticks;
    LivingEntity initiator;
    UUID initiatorUUID;
    Ritual ritual;
    int ticksProcessing;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(BLBlockEntities.ALTAR, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);

        if (nbt.containsUuid("Initiator"))
            this.initiatorUUID = nbt.getUuid("Initiator");

        if (nbt.contains("Ritual")) {
            this.ticksProcessing = nbt.getInt("TicksProcessing");
            Ritual.RITUAL_CODEC.parse(NbtOps.INSTANCE, nbt.get("Ritual"))
              .resultOrPartial(Bloodlust.LOGGER::error)
              .ifPresent(ritual -> this.ritual = ritual);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);

        if (this.initiatorUUID != null)
            nbt.putUuid("Initiator", this.initiatorUUID);

        if (this.ritual != null) {
            nbt.putInt("TicksProcessing", this.ticksProcessing);
            Ritual.RITUAL_CODEC.encodeStart(NbtOps.INSTANCE, this.ritual)
              .resultOrPartial(Bloodlust.LOGGER::error)
              .ifPresent(tag -> nbt.put("Ritual", tag));
        }
    }

    public static void tickClient(World world, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        altar.ticks++;
        if (!state.get(AltarBlock.ACTIVE))
            return;

        BloodlustClient.isAltarActive = true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        if (altar.ritual == null)
            return;

        LivingEntity initiator = altar.getInitiator();
        if (!VampireHelper.isVampire(initiator)) {
            altar.ritual = null;
            altar.ticksProcessing = 0;
            altar.setInitiator(null);
            altar.markDirty();
            return;
        }

        if (world.getTime() % 20 == 0) {
            // todo: this crashes on the dedicated server
            world.playSound(null, pos, BLSounds.ALTAR_BEATS, SoundCategory.BLOCKS);
            SpawnAltarBeatParticleS2C packet = new SpawnAltarBeatParticleS2C(altar.pos);
            PlayerLookup.tracking(altar)
              .forEach(player -> ServerPlayNetworking.send(player, packet));
        }

        if (altar.ticksProcessing < 300) {
            altar.ticksProcessing++;
            return;
        }

        altar.ritual.onCompleted(world, initiator, pos, altar);
        if (initiator instanceof ServerPlayerEntity player)
            BLAdvancementCriterion.PERFORM_RITUAL.trigger(player, altar.ritual);
        altar.getStack(0).decrement(1);
        altar.ritual = null;
        altar.ticksProcessing = 0;
        altar.setInitiator(null);
        world.setBlockState(pos, state.with(AltarBlock.ACTIVE, false));
        altar.markDirty();
    }

    public void startRitual(World world, LivingEntity initiator, BlockPos pos, BlockState state) {
        if (!VampireHelper.isVampire(initiator))
            return;

        List<BlockPos> pedestalPositions = new ArrayList<>();
        List<ItemStack> pedestalItems = new ArrayList<>();

        BlockPos.streamOutwards(pos, PEDESTAL_SEARCH_RADIUS, PEDESTAL_SEARCH_RADIUS, PEDESTAL_SEARCH_RADIUS)
          .map(position -> world.getBlockEntity(position, BLBlockEntities.PEDESTAL))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(pedestal -> {
              if (pedestal.getItem().isEmpty())
                  return;

              pedestalPositions.add(pedestal.getPos());
              pedestalItems.add(pedestal.getItem().copy());
          });

        SimpleInventory inventory = new SimpleInventory(pedestalItems.size() + 1);
        inventory.setStack(0, this.getStack(0));
        for (int i = 0; i < pedestalItems.size(); i++) {
            inventory.setStack(i + 1, pedestalItems.get(i));
        }

        world.getRecipeManager().getFirstMatch(BLRecipeTypes.ALTAR_RECIPE, inventory, world)
          .ifPresent(recipe -> {
              // send vfx packet
              AltarRecipeStartS2C packet = new AltarRecipeStartS2C(pos, pedestalPositions);
              PlayerLookup.tracking(this)
                .forEach(player -> ServerPlayNetworking.send(player, packet));

              pedestalPositions.forEach(position -> {
                  PedestalBlockEntity entity = ((PedestalBlockEntity) world.getBlockEntity(position));
                  if (entity == null)
                      return;
                  entity.getItem().decrement(1);
                  entity.inv.markDirty();
              });

              this.setInitiator(initiator);
              world.setBlockState(pos, state.with(AltarBlock.ACTIVE, true));
              this.ticksProcessing = 0;
              this.ritual = recipe.getRitual();
              if (initiator instanceof ServerPlayerEntity player) {
                  Criteria.RECIPE_CRAFTED.trigger(player, recipe.getId(), inventory.stacks);
              }
              this.markDirty();
          });
    }

    public void setInitiator(LivingEntity entity) {
        if (entity == null) {
            this.initiator = null;
            this.initiatorUUID = null;
            this.markDirty();
            return;
        }
        this.initiator = entity;
        this.initiatorUUID = entity.getUuid();
        this.markDirty();
    }

    public LivingEntity getInitiator() {
        if (this.initiator != null && this.initiator.isAlive())
            return this.initiator;
        if (this.initiatorUUID != null && this.getWorld() instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(this.initiatorUUID);
            if (entity instanceof LivingEntity livingEntity) {
                this.initiator = livingEntity;
                return this.initiator;
            }
            this.initiatorUUID = null;
            return null;
        }
        return null;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound compound = new NbtCompound();
        Inventories.writeNbt(compound, this.inventory);
        return compound;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        if (this.inventory.isEmpty())
            return true;
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty())
                return false;
        }
        return true;
    }

    public void onInventoryChanged() {
        this.markDirty();
        if (this.world != null)
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), AltarBlock.NOTIFY_LISTENERS);
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = Inventories.splitStack(this.inventory, slot, amount);
        if (!stack.isEmpty())
            this.onInventoryChanged();
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = this.inventory.get(slot);
        this.inventory.set(slot, ItemStack.EMPTY);
        if (!stack.isEmpty())
            this.onInventoryChanged();
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        this.onInventoryChanged();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.inventory.clear();
        this.onInventoryChanged();
    }


    @Override
    public ItemStack getDisplayItem() {
        return this.inventory.get(0);
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
