package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.function.Consumer;

/**
 * Represents a VampireAbility that can send data of type <code>T</code> to the client
 * <br> To use, you must implement writePacket, readPacket, and handle.
 * <br> To send data to the client you must call <code>SyncableVampireComponent#sync</code> and provide data of the correct type.
 *
 * @param <T> the type of data to be sent to the client. Can be anything, as long as it can be written to and read from a packet
 */
public interface SyncableVampireAbility<T> {
    default void writePacket(PacketByteBuf buf, World world, T data) {
    }

    default T readPacket(PacketByteBuf buf, World entity) {
        return null;
    }

    default void sync(LivingEntity entity, T data) {
        if (!VampireAbility.class.isAssignableFrom(this.getClass()))
            throw new IllegalStateException("SyncableVampireAbility must be implemented on a VampireAbility!");
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entity.getId());
        buf.writeRegistryValue(BLRegistries.VAMPIRE_ABILITIES, (VampireAbility) this);
        writePacket(buf, entity.getWorld(), data);
        PlayerLookup.tracking(entity).forEach(p ->
            ServerPlayNetworking.send(p, BLResources.ABILITY_SYNC_CHANNEL, buf)
                                             );
        if (entity instanceof ServerPlayerEntity p)
            ServerPlayNetworking.send(p, BLResources.ABILITY_SYNC_CHANNEL, buf);
    }

    default void handlePacket(LivingEntity entity, PacketByteBuf buf, Consumer<Runnable> executor) {
        T data = readPacket(buf, entity.getWorld());
        executor.accept(() -> handle(entity, data));
    }

    void handle(LivingEntity entity, T data);
}
