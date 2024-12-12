package com.auroali.sanguinisluxuria.common.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

import java.util.Locale;

public class DelayedParticleEffect implements ParticleEffect {
    public static final Codec<DelayedParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Registries.PARTICLE_TYPE.getCodec().fieldOf("type").forGetter(DelayedParticleEffect::getType),
      Codec.INT.fieldOf("delay").forGetter(DelayedParticleEffect::getDelay)
    ).apply(instance, DelayedParticleEffect::new));
    public static final ParticleEffect.Factory<DelayedParticleEffect> FACTORY = new Factory<>() {
        @Override
        public DelayedParticleEffect read(ParticleType<DelayedParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int delay = reader.readInt();
            return new DelayedParticleEffect(type, delay);
        }

        @Override
        public DelayedParticleEffect read(ParticleType<DelayedParticleEffect> type, PacketByteBuf buf) {
            int delay = buf.readVarInt();
            return new DelayedParticleEffect(type, delay);
        }
    };

    int delay;
    ParticleType<?> type;

    public DelayedParticleEffect(ParticleType<?> type, int i) {
        this.type = type;
        this.delay = i;
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.delay);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %d", Registries.PARTICLE_TYPE.getId(this.getType()), this.delay);
    }

    public int getDelay() {
        return this.delay;
    }
}
