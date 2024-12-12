package com.auroali.sanguinisluxuria.client.particles;

import com.auroali.sanguinisluxuria.common.particles.DelayedParticleEffect;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AltarBeatParticle extends SpriteBillboardParticle {
    int delay;

    protected AltarBeatParticle(ClientWorld clientWorld, double d, double e, double f, int delay) {
        super(clientWorld, d, e, f);
        this.gravityStrength = 0;
        this.collidesWithWorld = false;
        this.delay = delay;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (this.delay > 0)
            return;
        Vec3d cameraPos = camera.getPos();
        float x = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - cameraPos.getX());
        float y = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - cameraPos.getY());
        float z = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - cameraPos.getZ());

        Vector3f[] vertices = new Vector3f[]{
          new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
        };
        Quaternionf rotation = new Quaternionf().rotationX(MathHelper.HALF_PI);
        float size = this.getSize(tickDelta);

        for (int i = 0; i < 4; i++) {
            Vector3f vector3f = vertices[i];
            vector3f.rotate(rotation);
            vector3f.mul(size);
            vector3f.add(x, y, z);
        }

        float k = this.getMinU();
        float l = this.getMaxU();
        float m = this.getMinV();
        float n = this.getMaxV();
        int o = this.getBrightness(tickDelta);
        vertexConsumer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
          .texture(l, n)
          .color(this.red, this.green, this.blue, this.alpha)
          .light(o)
          .next();
        vertexConsumer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
          .texture(l, m)
          .color(this.red, this.green, this.blue, this.alpha)
          .light(o)
          .next();
        vertexConsumer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
          .texture(k, m)
          .color(this.red, this.green, this.blue, this.alpha)
          .light(o)
          .next();
        vertexConsumer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
          .texture(k, n)
          .color(this.red, this.green, this.blue, this.alpha)
          .light(o)
          .next();
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale * MathHelper.clamp((this.age + tickDelta) / (this.maxAge * 0.25f), 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        if (this.delay > 0) {
            this.delay--;
            return;
        }
        super.tick();
        float toRemove = 1.f / this.maxAge;
        if (this.alpha >= toRemove)
            this.alpha -= toRemove;
        else this.markDead();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<DelayedParticleEffect> {
        final SpriteProvider sprites;

        public Factory(SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(DelayedParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            AltarBeatParticle particle = new AltarBeatParticle(world, x, y, z, parameters.getDelay());
            particle.setVelocity(velocityX, velocityY, velocityZ);
            particle.setSprite(this.sprites);
            particle.scale = 1.2f;
            particle.maxAge = 11;
            return particle;
        }
    }
}
