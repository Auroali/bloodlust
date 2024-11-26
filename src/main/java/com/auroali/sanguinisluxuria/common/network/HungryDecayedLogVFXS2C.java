package com.auroali.sanguinisluxuria.common.network;

import com.auroali.sanguinisluxuria.BLResources;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record HungryDecayedLogVFXS2C(int entityId) implements FabricPacket {
    public static final PacketType<HungryDecayedLogVFXS2C> ID = PacketType.create(BLResources.HUNGRY_DECAYED_LOG_VFX_S2C, HungryDecayedLogVFXS2C::new);

    public HungryDecayedLogVFXS2C(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }

}
