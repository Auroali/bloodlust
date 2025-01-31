package com.auroali.sanguinisluxuria.common.blockentities;

import com.auroali.sanguinisluxuria.BloodlustClient;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.blocks.AltarBlock;
import com.auroali.sanguinisluxuria.common.network.AltarRecipeStartS2C;
import com.auroali.sanguinisluxuria.common.network.SpawnAltarBeatParticleS2C;
import com.auroali.sanguinisluxuria.common.registry.BLAdvancementCriterion;
import com.auroali.sanguinisluxuria.common.registry.BLBlockEntities;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeTypes;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.rituals.ActiveRitualData;
import com.auroali.sanguinisluxuria.common.rituals.Ritual;
import com.auroali.sanguinisluxuria.common.rituals.RitualParameters;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AltarBlockEntity extends BlockEntity implements Inventory, ItemDisplayingBlockEntity {
    public static final int INVENTORY_SIZE = 1;
    public static final int PEDESTAL_SEARCH_RADIUS = 8;
    DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    int ticks;
    LivingEntity cachedInitiator;
    LivingEntity cachedTarget;
    ActiveRitualData ritualData;
    int ticksProcessing;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(BLBlockEntities.ALTAR, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);

        this.ritualData = ActiveRitualData.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);

        ActiveRitualData.writeNbt(nbt, this.ritualData);
    }

    public static void tickClient(World world, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        altar.ticks++;
        if (!state.get(AltarBlock.ACTIVE))
            return;

        BloodlustClient.isAltarActive = true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        if (altar.ritualData == null)
            return;

        LivingEntity initiator = altar.getInitiator();
        LivingEntity target = altar.getTarget();
        if (!VampireHelper.isVampire(initiator) || target == null) {
            altar.ritualData = null;
            altar.ticksProcessing = 0;
            altar.markDirty();
            return;
        }

        if (world.getTime() % 20 == 0) {
            world.playSound(null, pos, BLSounds.ALTAR_BEATS, SoundCategory.BLOCKS);
            SpawnAltarBeatParticleS2C packet = new SpawnAltarBeatParticleS2C(altar.pos);
            PlayerLookup.tracking(altar)
              .forEach(player -> ServerPlayNetworking.send(player, packet));
        }

        if (altar.ticksProcessing < 300) {
            altar.ticksProcessing++;
            return;
        }

        Ritual ritual = altar.ritualData.ritual();
        RitualParameters parameters = RitualParameters
          .builder()
          .world(world)
          .position(pos)
          .inventory(altar)
          .initiator(initiator)
          .target(target)
          .build();
        ritual.onCompleted(parameters);
        parameters.applyToPlayerInitiator(player -> BLAdvancementCriterion.PERFORM_RITUAL.trigger(player, ritual));
        altar.getStack(0).decrement(1);
        altar.ritualData = null;
        altar.ticksProcessing = 0;
        altar.cachedTarget = null;
        altar.cachedInitiator = null;
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

              world.setBlockState(pos, state.with(AltarBlock.ACTIVE, true));
              this.ticksProcessing = 0;
              this.ritualData = new ActiveRitualData(recipe.getRitual(), initiator.getUuid(), initiator.getUuid());
              if (initiator instanceof ServerPlayerEntity player) {
                  Criteria.RECIPE_CRAFTED.trigger(player, recipe.getId(), inventory.stacks);
              }
              this.markDirty();
          });
    }

    public LivingEntity getInitiator() {
        if (this.cachedInitiator == null || !this.cachedInitiator.isAlive()) {
            this.cachedInitiator = null;
            if (this.ritualData != null)
                return this.cachedInitiator = this.ritualData.resolveInitiator(this.getWorld());
            return null;
        }
        return this.cachedInitiator;
    }

    public LivingEntity getTarget() {
        if (this.cachedTarget == null || !this.cachedTarget.isAlive()) {
            this.cachedTarget = null;
            if (this.ritualData != null)
                return this.cachedTarget = this.ritualData.resolveTarget(this.getWorld());
            return null;
        }
        return this.cachedTarget;
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
        this.ritualData = null;
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
