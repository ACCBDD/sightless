package com.accbdd.sightless.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class LidarParticle extends TextureSheetParticle {

    protected LidarParticle(ClientLevel level, double x, double y, double z, double r, double g, double b, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.gravity = 0;
        this.lifetime = 500;
        this.quadSize = 0.03f;

        this.rCol = (float) r;
        this.gCol = (float) g;
        this.bCol = (float) b;
        this.alpha = 1.0F;

        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return LidarParticleRenderType.LIDAR_TARGET;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    public static class LidarParticleProvider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public LidarParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new LidarParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
