package com.auroali.sanguinisluxuria.common.network;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record ActivateAbilityC2S(VampireAbility ability) implements FabricPacket {
    public static final PacketType<ActivateAbilityC2S> ID = PacketType.create(BLResources.ACTIVATE_ABILITY_C2S, ActivateAbilityC2S::new);

    public ActivateAbilityC2S(PacketByteBuf buf) {
        this(buf.readRegistryValue(BLRegistries.VAMPIRE_ABILITIES));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeRegistryValue(BLRegistries.VAMPIRE_ABILITIES, this.ability());
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
