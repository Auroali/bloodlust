package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import com.auroali.sanguinisluxuria.common.registry.BLVampireAbilities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.event.GameEvent;

import java.util.function.Supplier;

public class VampireTeleportAbility extends VampireAbility implements SyncableVampireAbility<VampireTeleportAbility.TeleportData> {
    public VampireTeleportAbility(Supplier<ItemStack> icon, VampireAbility parent) {
        super(icon, parent);
    }

    @Override
    public void tick(LivingEntity entity, VampireComponent component, BloodComponent blood) {

    }

    @Override
    public boolean isKeybindable() {
        return true;
    }

    @Override
    public boolean activate(LivingEntity entity, VampireComponent component) {
        if(component.getAbilties().isOnCooldown(this))
            return false;

        Vec3d start = entity.getPos();
        BlockHitResult result = entity.world.raycast(new RaycastContext(
                entity.getEyePos(),
                entity.getEyePos().add(entity.getRotationVector().multiply(getRange(component.getAbilties()))),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        ));

        if(result == null)
            return false;

        if(entity.hasVehicle())
            entity.stopRiding();

        BlockPos pos = result.getBlockPos().offset(result.getSide());
        Vec3d newPos = new Vec3d(pos.getX() + 0.5f, result.getPos().getY(), pos.getZ() + 0.5f);
        entity.teleport(newPos.getX(), newPos.getY(), newPos.getZ());
        entity.world.emitGameEvent(GameEvent.TELEPORT, start, GameEvent.Emitter.of(entity));
        entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        sync(entity, new TeleportData(start, entity.getPos()));

        component.getAbilties().setCooldown(this, getCooldown(component.getAbilties()));
        return true;
    }

    public double getRange(VampireAbilityContainer container) {
        double range = 8;
        for(VampireAbility ability : container) {
            if(ability.isIn(BLTags.VampireAbilities.TELEPORT_RANGE))
                range += 4;
        }
        return range;
    }
    public int getCooldown(VampireAbilityContainer container) {
        int cooldown = 250;
        if(container.hasAbility(BLVampireAbilities.TELEPORT_COOLDOWN_1))
            cooldown -= 75;
        if(container.hasAbility(BLVampireAbilities.TELEPORT_COOLDOWN_2))
            cooldown -= 75;
        return cooldown;
    }
    @Override
    public boolean canTickCooldown(LivingEntity entity, VampireComponent vampireComponent) {
        return entity.isOnGround();
    }

    @Override
    public void writePacket(PacketByteBuf buf, LivingEntity entity, TeleportData data) {
        buf.writeDouble(data.from.x);
        buf.writeDouble(data.from.y);
        buf.writeDouble(data.from.z);
        buf.writeDouble(data.to.x);
        buf.writeDouble(data.to.y);
        buf.writeDouble(data.to.z);
    }

    @Override
    public TeleportData readPacket(PacketByteBuf buf, LivingEntity entity) {
        double fromX = buf.readDouble();
        double fromY = buf.readDouble();
        double fromZ = buf.readDouble();
        double toX = buf.readDouble();
        double toY = buf.readDouble();
        double toZ = buf.readDouble();
        return new TeleportData(
                new Vec3d(fromX, fromY, fromZ),
                new Vec3d(toX, toY, toZ)
        );
    }

    @Override
    public void handle(LivingEntity entity, TeleportData data) {
        int dist = (int) data.to.distanceTo(data.from);
        double yOffset = entity.getEyeHeight(entity.getPose()) / 2;
        for(int i = 0; i < dist; i++) {
            Vec3d pos = data.from.lerp(data.to, (float) i / dist).add(0, yOffset, 0);
            entity.world.addParticle(
                    DustParticleEffect.DEFAULT,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    0,
                    0,
                    0
            );
        }
    }

    public record TeleportData(Vec3d from, Vec3d to) {}
}
