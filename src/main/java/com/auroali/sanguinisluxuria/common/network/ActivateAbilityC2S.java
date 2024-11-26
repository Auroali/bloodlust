package com.auroali.sanguinisluxuria.common.network;

import com.auroali.sanguinisluxuria.BLResources;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record ActivateAbilityC2S(int abilitySlot) implements FabricPacket {
    public static final PacketType<ActivateAbilityC2S> ID = PacketType.create(BLResources.ACTIVATE_ABILITY_C2S, ActivateAbilityC2S::new);

    public ActivateAbilityC2S(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.abilitySlot);
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
