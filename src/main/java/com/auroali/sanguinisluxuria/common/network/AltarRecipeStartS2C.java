package com.auroali.sanguinisluxuria.common.network;

import com.auroali.sanguinisluxuria.BLResources;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record AltarRecipeStartS2C(BlockPos pos, List<BlockPos> pedestals) implements FabricPacket {
    public static final PacketType<AltarRecipeStartS2C> ID = PacketType.create(BLResources.ALTAR_RECIPE_START_S2C, AltarRecipeStartS2C::new);

    public AltarRecipeStartS2C(PacketByteBuf buf) {
        this(buf.readBlockPos(), buf.readList(PacketByteBuf::readBlockPos));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeCollection(this.pedestals, PacketByteBuf::writeBlockPos);
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
