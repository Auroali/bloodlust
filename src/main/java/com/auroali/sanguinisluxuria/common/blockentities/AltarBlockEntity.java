package com.auroali.sanguinisluxuria.common.blockentities;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.BloodlustClient;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.blocks.AltarBlock;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.network.AltarRecipeStartS2C;
import com.auroali.sanguinisluxuria.common.recipes.AltarInventory;
import com.auroali.sanguinisluxuria.common.recipes.AltarRecipe;
import com.auroali.sanguinisluxuria.common.registry.BLBlockEntities;
import com.auroali.sanguinisluxuria.common.registry.BLRecipeTypes;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AltarBlockEntity extends BlockEntity {
    final SimpleInventory internalInventory = new SimpleInventory(ItemStack.EMPTY);
    DefaultedList<ItemStack> recipeItems = DefaultedList.of();
    AltarRecipe recipe = null;
    Identifier recipeId = null;
    int ticksProcessing = 0;
    public int ticks;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(BLBlockEntities.SKILL_UPGRADER, pos, state);
        this.internalInventory.addListener(inv -> {
            if (world != null && !world.isClient) {
                this.recipe = null;
                this.recipeId = null;
                for (ItemStack stack : this.recipeItems) {
                    ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
                    world.spawnEntity(item);
                }
                this.recipeItems.clear();
                world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
            }
        });
    }

    public void checkAndStartRecipe(World world, LivingEntity entity) {
        if (recipe != null || world.isClient || !VampireHelper.isVampire(entity))
            return;

        DefaultedList<ItemStack> collectedStacks = DefaultedList.of();
        List<PedestalBlockEntity> pedestals = new ArrayList<>();
        BlockPos.stream(new Box(pos).expand(15))
          .forEach(p -> {
              BlockEntity bl = world.getBlockEntity(p);
              if (bl instanceof PedestalBlockEntity pedestal && !pedestal.getItem().isEmpty()) {
                  ItemStack stack = pedestal.getItem().copy();
                  stack.setCount(1);
                  collectedStacks.add(stack);
                  pedestals.add(pedestal);
              }
          });

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(entity);

        AltarRecipe recipe = world.getRecipeManager().values().stream()
          .filter(r -> r.getType().equals(BLRecipeTypes.ALTAR_RECIPE))
          .map(AltarRecipe.class::cast)
          .filter(p -> p.matches(vampire.getLevel(), this.internalInventory.getStack(0), collectedStacks))
          .findFirst().orElse(null);

        if (recipe == null)
            return;

        pedestals.forEach(p -> {
            p.getItem().decrement(1);
            p.markDirty();
            BlockState state = world.getBlockState(p.getPos());
            world.updateListeners(p.getPos(), state, state, Block.NOTIFY_LISTENERS);
        });


        PlayerLookup.tracking(this)
          .forEach(p -> ServerPlayNetworking.send(p, new AltarRecipeStartS2C(pos, pedestals.stream().map(BlockEntity::getPos).toList())));
        this.recipe = recipe;
        this.recipeItems = collectedStacks;
        this.ticksProcessing = 0;
        markDirty();

        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(AltarBlock.ACTIVE, true));
    }

    public ItemStack getCatalyst() {
        return this.internalInventory.getStack(0);
    }

    public static void vfxTick(World world, BlockPos pos, BlockState state, AltarBlockEntity entity) {
        entity.ticks++;
        if (!state.get(AltarBlock.ACTIVE)) {
            return;
        }

        BloodlustClient.isAltarActive = true;

        Box box = new Box(pos).expand(5);
        for (int i = 0; i < 4; i++) {
            double x = box.minX + world.random.nextDouble() * box.getXLength();
            double y = box.minY + world.random.nextDouble() * box.getYLength();
            double z = box.minZ + world.random.nextDouble() * box.getZLength();
            world.addParticle(DustParticleEffect.DEFAULT, x, y, z, 0, 0, 0);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, AltarBlockEntity entity) {
        if (entity.recipeId != null)
            entity.validateRecipe();

        if (entity.recipe != null) {
            if (entity.ticksProcessing < entity.recipe.getProcessingTicks()) {
                if (entity.ticksProcessing % 20 == 0)
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), BLSounds.ALTAR_BEATS, SoundCategory.BLOCKS, 1.0f, 1.0f);
                entity.ticksProcessing++;
                entity.markDirty();
                return;
            }

            AltarInventory inv = new AltarInventory(entity.recipeItems.size());
            for (int i = 0; i < entity.recipeItems.size(); i++) {
                inv.setStack(i, entity.recipeItems.get(i));
            }

            DefaultedList<ItemStack> outputs = DefaultedList.of();
            outputs.add(entity.recipe.craft(inv, world.getRegistryManager()));
            outputs.addAll(entity.recipe.getRemainder(inv).stream().filter(i -> !i.isEmpty()).toList());

            for (ItemStack stack : outputs) {
                ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
                world.spawnEntity(item);
            }

            entity.recipe = null;
            entity.recipeItems.clear();
            entity.ticksProcessing = 0;
            entity.internalInventory
              .removeStack(0, 1);
            entity.markDirty();
            world.setBlockState(pos, state.with(AltarBlock.ACTIVE, false));
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Recipe")) {
            Identifier recipeId = Identifier.tryParse(nbt.getString("Recipe"));
            if (recipeId == null) {
                Bloodlust.LOGGER.warn("Failed to parse id");
                return;
            }

            this.recipeId = recipeId;
            this.ticksProcessing = nbt.getInt("ProcessingTicks");
            NbtList inv = nbt.getList("RecipeItems", NbtElement.COMPOUND_TYPE);
            inv.stream()
              .map(NbtCompound.class::cast)
              .map(ItemStack::fromNbt)
              .forEach(recipeItems::add);

        }
        // todo: this isnt updating properly when recipes are completed
        Inventories.readNbt(nbt, this.internalInventory.stacks);
    }

    private void validateRecipe() {
        if (world == null || (recipe = (AltarRecipe) world.getRecipeManager().get(recipeId).orElse(null)) == null) {
            resetRecipeState();
            return;
        }
        recipeId = null;
    }

    void resetRecipeState() {
        recipeItems.clear();
        ticksProcessing = 0;
        recipeId = null;
        markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (recipe != null) {
            nbt.putString("Recipe", recipe.getId().toString());
            NbtList inv = new NbtList();
            recipeItems.forEach(i -> inv.add(i.writeNbt(new NbtCompound())));
            nbt.put("RecipeItems", inv);
            nbt.putInt("ProcessingTicks", ticksProcessing);
        }
        Inventories.writeNbt(nbt, this.internalInventory.stacks);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound data = new NbtCompound();
        Inventories.writeNbt(data, this.internalInventory.stacks, true);
        return data;
    }

    public void setCatalyst(ItemStack stack) {
        this.internalInventory.setStack(0, stack);
    }

    public Inventory getInventory() {
        return this.internalInventory;
    }

    public DefaultedList<ItemStack> getRecipeItems() {
        return this.recipeItems;
    }
}
