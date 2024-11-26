package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLDamageSources;
import com.auroali.sanguinisluxuria.common.registry.BLEntityAttributes;
import com.auroali.sanguinisluxuria.common.registry.BLItems;
import com.auroali.sanguinisluxuria.config.BLConfig;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.function.Supplier;

public class VampireTeleportAbility extends VampireAbility implements SyncableVampireAbility<VampireTeleportAbility.TeleportData> {
    public VampireTeleportAbility(Supplier<ItemStack> icon, VampireAbility parent) {
        super(icon, parent);
    }

    @Override
    public boolean isKeybindable() {
        return true;
    }

    @Override
    public void activate(LivingEntity entity, VampireComponent component) {
        if (component.getAbilties().isOnCooldown(this))
            return;

        Vec3d start = entity.getPos();
        BlockHitResult result = entity.getWorld().raycast(new RaycastContext(
          entity.getEyePos(),
          entity.getEyePos().add(entity.getRotationVector().multiply(this.getRange(entity))),
          RaycastContext.ShapeType.COLLIDER,
          RaycastContext.FluidHandling.NONE,
          entity
        ));

        if (result == null)
            return;

        if (entity.hasVehicle())
            entity.stopRiding();

        BlockPos pos = result.getBlockPos().offset(result.getSide());
        Vec3d newPos = new Vec3d(pos.getX() + 0.5f, result.getPos().getY(), pos.getZ() + 0.5f);
        entity.teleport(newPos.getX(), newPos.getY(), newPos.getZ());
        entity.fallDistance = 0;
        entity.getWorld().emitGameEvent(GameEvent.TELEPORT, start, GameEvent.Emitter.of(entity));
        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        TrinketsApi.getTrinketComponent(entity).ifPresent(c -> {
            if (c.isEquipped(BLItems.PENDANT_OF_PIERCING))
                this.damageEntitiesBetween(entity, start, newPos);
        });


        this.sync(entity, new TeleportData(start, entity.getPos()));

        component.getAbilties().setCooldown(this, this.getCooldown(entity));
    }

    public void damageEntitiesBetween(LivingEntity entity, Vec3d start, Vec3d end) {
        Box box = new Box(start, end);
        entity.getWorld().getOtherEntities(entity, box, e -> e.isLiving() && e.isAlive())
          .forEach(e -> {
              if (e.getBoundingBox().intersects(start, end) && e.damage(BLDamageSources.teleport(entity), 4) && entity instanceof PlayerEntity player)
                  player.addExhaustion(BLConfig.INSTANCE.piercingExhaustion / BLConfig.INSTANCE.vampireExhaustionMultiplier);
          });
    }

    public double getRange(LivingEntity entity) {
        return entity.getAttributeValue(BLEntityAttributes.BLINK_RANGE);
    }

    public int getCooldown(LivingEntity entity) {
        return (int) entity.getAttributeValue(BLEntityAttributes.BLINK_COOLDOWN);
    }

    @Override
    public boolean canTickCooldown(LivingEntity entity, VampireComponent vampireComponent) {
        return entity.isOnGround();
    }

    @Override
    public void writePacket(PacketByteBuf buf, World world, TeleportData data) {
        buf.writeDouble(data.from.x);
        buf.writeDouble(data.from.y);
        buf.writeDouble(data.from.z);
        buf.writeDouble(data.to.x);
        buf.writeDouble(data.to.y);
        buf.writeDouble(data.to.z);
    }

    @Override
    public TeleportData readPacket(PacketByteBuf buf, World world) {
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
        int dist = (int) data.to.distanceTo(data.from) * 2;
        double eyeYOffset = entity.getEyeHeight(entity.getPose()) / 2;
        Random rand = entity.getRandom();
        for (int i = 0; i < dist; i++) {
            Vec3d pos = data.from.lerp(data.to, (float) i / dist).add(0, eyeYOffset, 0);
            double xOffset = rand.nextGaussian() * 0.2;
            double yOffset = rand.nextGaussian() * 0.2;
            double zOffset = rand.nextGaussian() * 0.2;
            entity.getWorld().addParticle(
              DustParticleEffect.DEFAULT,
              pos.getX() + xOffset,
              pos.getY() + yOffset,
              pos.getZ() + zOffset,
              0,
              0,
              0
            );
        }
    }

    public record TeleportData(Vec3d from, Vec3d to) {
    }
}
