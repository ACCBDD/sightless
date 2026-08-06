package com.accbdd.sightless.client.particle;

import com.accbdd.sightless.Sightless;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class LidarParticleRenderType {
    public static final ParticleRenderType LIDAR_TARGET = new ParticleRenderType() {

        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderTarget lidarTarget = Sightless.ClientModEvents.getBlackoutChain().getTempTarget("lidar_target");
            lidarTarget.bindWrite(false);

            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "sightless:lidar_target";
        }
    };
}
