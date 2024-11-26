package com.auroali.sanguinisluxuria.common.network;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record BindAbilityC2S(VampireAbility ability, int slot) implements FabricPacket {
    public static final PacketType<BindAbilityC2S> ID = PacketType.create(BLResources.BIND_ABILITY_C2S, BindAbilityC2S::new);

    public BindAbilityC2S(PacketByteBuf buf) {
        this(buf.readRegistryValue(BLRegistries.VAMPIRE_ABILITIES), buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeRegistryValue(BLRegistries.VAMPIRE_ABILITIES, this.ability);
        buf.writeVarInt(this.slot);
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
