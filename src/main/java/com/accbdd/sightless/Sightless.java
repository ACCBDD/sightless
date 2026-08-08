package com.accbdd.sightless;

import com.accbdd.sightless.block.SightedBlockEntity;
import com.accbdd.sightless.client.SightlessKeys;
import com.accbdd.sightless.client.particle.LidarParticle;
import com.accbdd.sightless.register.*;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.FloatBuffer;

@Mod(Sightless.MODID)
public class Sightless {
    public static final String MODID = "sightless";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Sightless(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        SightlessItems.ITEMS.register(modEventBus);
        SightlessBlocks.BLOCKS.register(modEventBus);
        SightlessBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        SightlessCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);
        SightlessParticleTypes.PARTICLE_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        private static PostChain blackoutChain;
        private static PostPass blackScreenPass;
        private static int sphereDataTexId;
        private static final int MAX_SPHERES = 1024; // plenty of headroom, cheap to allocate

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
                @Override
                protected Void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
                    return null;
                }

                @Override
                protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
                    reloadChain();
                }
            });
        }

        @SubscribeEvent
        public static void onChunkUnload(ChunkEvent.Unload event) {
            if (!event.getLevel().isClientSide()) return;
            SightedBlockEntity.ACTIVE.removeIf(be ->
                    new ChunkPos(be.getBlockPos()).equals(event.getChunk().getPos()));
        }

        @SubscribeEvent
        public static void onLevelUnload(LevelEvent.Unload event) {
            if (event.getLevel().isClientSide()) {
                SightedBlockEntity.ACTIVE.clear();
            }
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(SightlessParticleTypes.LIDAR_PARTICLE.get(), LidarParticle.LidarParticleProvider::new);
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (!SightlessKeys.TOGGLE_SHADER_KEY.isDown() && blackoutChain != null && event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                RenderTarget lidarTarget = getBlackoutChain().getTempTarget("lidar_target");
                lidarTarget.setClearColor(0, 0, 0, 0);
                lidarTarget.clear(false);
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            }

            if (!SightlessKeys.TOGGLE_SHADER_KEY.isDown() && blackoutChain != null && event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                Matrix4f proj = new Matrix4f(RenderSystem.getProjectionMatrix());
                Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
                Matrix4f invViewProj = proj.mul(modelView, new Matrix4f()).invert();
                Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
                if (blackoutChain.screenWidth != main.width || blackoutChain.screenHeight != main.height) {
                    resizeChain();
                }

                int sphereCount = Math.min(SightedBlockEntity.ACTIVE.size(), MAX_SPHERES);
                FloatBuffer buf = MemoryUtil.memAllocFloat(sphereCount * 4);
                int written = 0;
                for (SightedBlockEntity be : SightedBlockEntity.ACTIVE) {
                    if (written >= sphereCount) break;
                    Vec3 rel = Vec3.atCenterOf(be.getBlockPos()).subtract(camPos);
                    float r = be.getRevealRadius();
                    buf.put((float) rel.x).put((float) rel.y).put((float) rel.z).put(r * r);
                    written++;
                }
                buf.flip();

                if (written > 0) {
                    GlStateManager._bindTexture(sphereDataTexId);
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, written, 1, GL11.GL_RGBA, GL11.GL_FLOAT, buf);
                }
                MemoryUtil.memFree(buf);

                EffectInstance effect = blackScreenPass.effect;
                effect.safeGetUniform("InvViewProjMat").set(invViewProj);
                effect.safeGetUniform("SphereCount").set(written);
                blackoutChain.process(event.getPartialTick().getGameTimeDeltaPartialTick(true));
            }
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(SightlessKeys.TOGGLE_SHADER_KEY);
        }

        private static void reloadChain() {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                blackoutChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), ResourceLocation.fromNamespaceAndPath(MODID, "shaders/post/sightless.json"));
                createSphereTexture();
                blackScreenPass = blackoutChain.passes.get(0);
                blackScreenPass.addAuxAsset("SphereSampler", () -> sphereDataTexId, MAX_SPHERES, 1);
                resizeChain();
            } catch (IOException exception) {
                LOGGER.error("Failed to load blackout shader", exception);
            }
        }

        private static void resizeChain() {
            Minecraft minecraft = Minecraft.getInstance();
            blackoutChain.resize(minecraft.getMainRenderTarget().width, minecraft.getMainRenderTarget().height);
        }

        private static void createSphereTexture() {
            sphereDataTexId = GlStateManager._genTexture();
            GlStateManager._bindTexture(sphereDataTexId);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, MAX_SPHERES, 1, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer) null);
        }

        public static PostChain getBlackoutChain() {
            if (blackoutChain == null) {
                reloadChain();
            }
            return blackoutChain;
        }
    }
}
