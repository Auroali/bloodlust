package com.auroali.sanguinisluxuria.common.network;

import com.auroali.sanguinisluxuria.BLResources;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public record SpawnAltarBeatParticleS2C(BlockPos pos) implements FabricPacket {
    public static final PacketType<SpawnAltarBeatParticleS2C> ID = PacketType.create(BLResources.SPAWN_ALTAR_BEAT_PARTICLE_S2C, SpawnAltarBeatParticleS2C::new);

    public SpawnAltarBeatParticleS2C(PacketByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos());
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
