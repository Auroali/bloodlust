package com.auroali.sanguinisluxuria.common.abilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class InfectiousAbility extends VampireAbility implements SyncableVampireAbility<InfectiousAbility.InfectiousData> {
    public InfectiousAbility(Supplier<ItemStack> icon, VampireAbility parent) {
        super(icon, parent);
    }

    @Override
    public void writePacket(PacketByteBuf buf, World world, InfectiousData data) {
        buf.writeVarInt(data.target.getId());
        buf.writeVarInt(data.colours.size());
        for (Vector3f vec : data.colours) {
            buf.writeFloat(vec.x());
            buf.writeFloat(vec.y());
            buf.writeFloat(vec.z());
        }
    }

    @Override
    public InfectiousData readPacket(PacketByteBuf buf, World world) {
        int id = buf.readVarInt();
        int size = buf.readVarInt();
        List<Vector3f> list = Arrays.asList(new Vector3f[size]);
        for (int i = 0; i < size; i++) {
            float r = buf.readFloat();
            float g = buf.readFloat();
            float b = buf.readFloat();
            list.set(i, new Vector3f(r, g, b));
        }
        LivingEntity target = (LivingEntity) world.getEntityById(id);
        return new InfectiousData(target, list);
    }

    @Override
    public void handle(LivingEntity entity, InfectiousData data) {
        List<Vector3f> colours = data.colours;
        if (colours.isEmpty())
            return;
        Box box = data.target.getBoundingBox();
        Random rand = data.target.getRandom();
        int max = 22;
        for (int i = 0; i < max; i++) {
            double x = box.minX + rand.nextDouble() * box.getXLength();
            double y = box.minY + rand.nextDouble() * box.getYLength();
            double z = box.minZ + rand.nextDouble() * box.getZLength();
            Vector3f colour = colours.get(rand.nextInt(colours.size()));
            data.target.getWorld().addParticle(
              ParticleTypes.ENTITY_EFFECT,
              x,
              y,
              z,
              colour.x(),
              colour.y(),
              colour.z()
                                              );
        }
    }

    public record InfectiousData(LivingEntity target, List<Vector3f> colours) {
        public static InfectiousData create(LivingEntity target, Collection<StatusEffectInstance> statusEffects) {
            List<Vector3f> colours = new ArrayList<>(statusEffects.size());
            for (StatusEffectInstance effect : statusEffects) {
                float r = (effect.getEffectType().getColor() >> 16 & 0xFF) / 255.0f;
                float g = (effect.getEffectType().getColor() >> 8 & 0xFF) / 255.0f;
                float b = (effect.getEffectType().getColor() & 0xFF) / 255.0f;
                Vector3f c = new Vector3f(r, g, b);
                if (!colours.contains(c))
                    colours.add(c);
            }
            return new InfectiousData(target, colours);
        }
    }
}
