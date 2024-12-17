package com.auroali.sanguinisluxuria.common.components;

import com.auroali.sanguinisluxuria.common.registry.BLEnchantments;
import com.auroali.sanguinisluxuria.mixin.PersistentProjectileEntityAccessor;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class BloodTransferComponent implements Component, AutoSyncedComponent {
    Entity latchedEntity = null;
    UUID latchedEntityId = null;
    final PersistentProjectileEntity holder;
    int bloodTransferLevel = -1;

    public BloodTransferComponent(PersistentProjectileEntity entity) {
        this.holder = entity;
    }

    public int getBloodTransferLevel() {
        if (this.bloodTransferLevel != -1)
            return this.bloodTransferLevel;

        this.bloodTransferLevel = 0;
        if (this.holder.getWorld().isClient)
            return this.bloodTransferLevel;
        this.bloodTransferLevel = EnchantmentHelper.getLevel(BLEnchantments.BLOOD_DRAIN, ((PersistentProjectileEntityAccessor) this.holder).sanguinisluxuria$asItemStack());
        return this.bloodTransferLevel;
    }

    public Entity getLatchedEntity() {
        if (this.holder.getWorld() instanceof ServerWorld world && this.latchedEntityId != null && (this.latchedEntity == null || !this.latchedEntity.isAlive() || this.latchedEntity.isRemoved())) {
            this.latchedEntity = null;
            Entity newEntity = world.getEntity(this.latchedEntityId);
            if (newEntity == null) {
                this.latchedEntity = null;
                this.latchedEntityId = null;
                BLEntityComponents.BLOOD_TRANSFER_COMPONENT.sync(this.holder);
                return null;
            }
            this.latchedEntity = newEntity;
            BLEntityComponents.BLOOD_TRANSFER_COMPONENT.sync(this.holder);
        }
        return this.latchedEntity;
    }

    public void setLatchedEntity(Entity entity) {
        this.latchedEntity = entity;
        this.latchedEntityId = null;
        if (entity != null)
            this.latchedEntityId = entity.getUuid();
        BLEntityComponents.BLOOD_TRANSFER_COMPONENT.sync(this.holder);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("LatchedEntity", NbtElement.INT_ARRAY_TYPE))
            this.latchedEntityId = tag.getUuid("LatchedEntity");
        this.getLatchedEntity();
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (this.latchedEntityId != null)
            tag.putUuid("LatchedEntity", this.latchedEntityId);
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(this.getBloodTransferLevel());
        if (this.latchedEntity != null)
            buf.writeVarInt(this.latchedEntity.getId());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.bloodTransferLevel = buf.readVarInt();
        if (buf.isReadable())
            this.latchedEntity = this.holder.getWorld().getEntityById(buf.readVarInt());
        else
            this.latchedEntity = null;
    }
}
