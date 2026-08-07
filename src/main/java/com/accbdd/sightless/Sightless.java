package com.accbdd.sightless;

import com.accbdd.sightless.client.SightedBlockEntityRenderer;
import com.accbdd.sightless.client.SightlessKeys;
import com.accbdd.sightless.client.particle.LidarParticle;
import com.accbdd.sightless.register.*;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.io.IOException;

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
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(SightlessParticleTypes.LIDAR_PARTICLE.get(), LidarParticle.LidarParticleProvider::new);
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (blackoutChain != null && event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                RenderTarget lidarTarget = getBlackoutChain().getTempTarget("lidar_target");
                lidarTarget.setClearColor(0, 0, 0, 0);
                lidarTarget.clear(false);
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }

            if (blackoutChain != null && event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && !SightlessKeys.TOGGLE_SHADER_KEY.isDown()) {
                blackoutChain.process(event.getPartialTick().getGameTimeDeltaPartialTick(true));
            }
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(SightlessKeys.TOGGLE_SHADER_KEY);
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                    SightlessBlockEntities.SIGHTED_BLOCK_ENTITY.get(),
                    SightedBlockEntityRenderer::new
            );
        }

        private static void reloadChain() {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                blackoutChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), ResourceLocation.fromNamespaceAndPath(MODID, "shaders/post/black_screen.json"));
                blackoutChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            } catch (IOException exception) {
                LOGGER.error("Failed to load blackout shader", exception);
            }
        }

        public static PostChain getBlackoutChain() {
            if (blackoutChain == null) {
                reloadChain();
            }
            return blackoutChain;
        }
    }
}
