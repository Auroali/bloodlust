package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemRitual implements Ritual {
    public static final Codec<ItemRitual> CODEC = RecordCodecBuilder.create(instance -> instance
      .group(
        ItemStack.CODEC.fieldOf("result").forGetter(ItemRitual::getOutputItem)
      ).apply(instance, ItemRitual::new)
    );
    ItemStack outputItem;

    public ItemRitual(ItemStack outputItem) {
        this.outputItem = outputItem;
    }

    public ItemRitual(ItemConvertible item, int output) {
        this(new ItemStack(item, output));
    }

    public ItemRitual(ItemConvertible item) {
        this(item, 1);
    }

    public ItemStack getOutputItem() {
        return this.outputItem;
    }

    @Override
    public void onCompleted(World world, LivingEntity initiator, BlockPos pos, Inventory inventory) {
        Vec3d centerPos = pos.toCenterPos();
        ItemEntity entity = new ItemEntity(
          world,
          centerPos.getX(),
          centerPos.getY() + 1,
          centerPos.getZ(),
          this.outputItem.copy());
        world.spawnEntity(entity);
    }

    @Override
    public RitualType<?> getType() {
        return BLRitualTypes.ITEM_RITUAL_TYPE;
    }
}
