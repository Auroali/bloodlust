package com.auroali.sanguinisluxuria.common.network;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record UnlockAbilityC2S(VampireAbility ability) implements FabricPacket {
    public static final PacketType<UnlockAbilityC2S> ID = PacketType.create(BLResources.UNLOCK_ABILITY_C2S, UnlockAbilityC2S::new);

    public UnlockAbilityC2S(PacketByteBuf buf) {
        this(buf.readRegistryValue(BLRegistries.VAMPIRE_ABILITIES));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeRegistryValue(BLRegistries.VAMPIRE_ABILITIES, this.ability);
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
