package com.accbdd.sightless.client;

import com.accbdd.sightless.block.SightedBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class SightedBlockEntityRenderer implements BlockEntityRenderer<SightedBlockEntity> {
    public SightedBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(SightedBlockEntity blockEntity, float v, PoseStack poseStack, MultiBufferSource bufferSource, int i, int i1) {
        poseStack.pushPose();
        VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lines());
        AABB box = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

        float red = 1.0F;
        float green = 0.0F;
        float blue = 0.0F;
        float alpha = 1.0F;

        LevelRenderer.renderLineBox(
                poseStack,
                lineBuffer,
                box,
                red, green, blue, alpha
        );

        poseStack.popPose();
    }
}
