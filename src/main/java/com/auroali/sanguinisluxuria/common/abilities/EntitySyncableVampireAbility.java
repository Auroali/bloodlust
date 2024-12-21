package com.auroali.sanguinisluxuria.common.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;

public interface EntitySyncableVampireAbility<T extends Entity> extends SyncableVampireAbility<T> {
    @Override
    default void writePacket(PacketByteBuf buf, World world, T data) {
        buf.writeVarInt(data.getId());
    }

    @SuppressWarnings("unchecked")
    @Override
    default T readPacket(PacketByteBuf buf, World world) {
        Entity readEntity = world.getEntityById(buf.readVarInt());
        return (T) readEntity;
    }
}
