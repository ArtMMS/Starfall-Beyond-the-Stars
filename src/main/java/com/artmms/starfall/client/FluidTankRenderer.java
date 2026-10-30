package com.artmms.starfall.client;

import com.artmms.starfall.block.entity.FluidTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Draws the liquid inside the Fluid Tank glass, in 16 levels (1 per bucket).
 * It uses the fluid's own "still" texture, which is animated, so the liquid
 * gently moves (water ripples, lava flows, oil shimmers).
 */
public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity> {
    // Inner box of the tank (inside the iron frame), in block units
    private static final float MIN_XZ = 2.5F / 16F;
    private static final float MAX_XZ = 13.5F / 16F;
    private static final float BOTTOM = 1.5F / 16F;
    private static final float TOP = 14.5F / 16F;

    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FluidTankBlockEntity tank, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int visualLevel = tank.getVisualLevel();
        FluidStack fluid = tank.getTank().getFluid();
        if (visualLevel <= 0 || fluid.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation stillTexture = extensions.getStillTexture(fluid);
        if (stillTexture == null) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);
        int color = extensions.getTintColor(fluid);
        // Glowing fluids (lava) are drawn at full brightness
        int light = fluid.getFluid().getFluidType().getLightLevel(fluid) > 0 ? LightTexture.FULL_BRIGHT : packedLight;

        float height = BOTTOM + (TOP - BOTTOM) * visualLevel / FluidTankBlockEntity.VISUAL_LEVELS;
        float fill = (height - BOTTOM) / (TOP - BOTTOM);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        PoseStack.Pose pose = poseStack.last();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        // Side faces only use part of the texture, so it doesn't look stretched
        float vSideTop = v1 - (v1 - v0) * fill;

        float x0 = MIN_XZ;
        float x1 = MAX_XZ;
        float z0 = MIN_XZ;
        float z1 = MAX_XZ;
        float y0 = BOTTOM;
        float y1 = height;

        // Top
        quad(consumer, pose, color, light, 0, 1, 0,
                x0, y1, z0, u0, v0,
                x0, y1, z1, u0, v1,
                x1, y1, z1, u1, v1,
                x1, y1, z0, u1, v0);
        // North (-Z)
        quad(consumer, pose, color, light, 0, 0, -1,
                x1, y1, z0, u0, vSideTop,
                x1, y0, z0, u0, v1,
                x0, y0, z0, u1, v1,
                x0, y1, z0, u1, vSideTop);
        // South (+Z)
        quad(consumer, pose, color, light, 0, 0, 1,
                x0, y1, z1, u0, vSideTop,
                x0, y0, z1, u0, v1,
                x1, y0, z1, u1, v1,
                x1, y1, z1, u1, vSideTop);
        // West (-X)
        quad(consumer, pose, color, light, -1, 0, 0,
                x0, y1, z0, u0, vSideTop,
                x0, y0, z0, u0, v1,
                x0, y0, z1, u1, v1,
                x0, y1, z1, u1, vSideTop);
        // East (+X)
        quad(consumer, pose, color, light, 1, 0, 0,
                x1, y1, z1, u0, vSideTop,
                x1, y0, z1, u0, v1,
                x1, y0, z0, u1, v1,
                x1, y1, z0, u1, vSideTop);
    }

    /** Draws a quad on both sides, so it is visible from inside and outside. */
    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int color, int light,
                             float nx, float ny, float nz,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv) {
        vertex(consumer, pose, ax, ay, az, au, av, color, light, nx, ny, nz);
        vertex(consumer, pose, bx, by, bz, bu, bv, color, light, nx, ny, nz);
        vertex(consumer, pose, cx, cy, cz, cu, cv, color, light, nx, ny, nz);
        vertex(consumer, pose, dx, dy, dz, du, dv, color, light, nx, ny, nz);

        vertex(consumer, pose, dx, dy, dz, du, dv, color, light, -nx, -ny, -nz);
        vertex(consumer, pose, cx, cy, cz, cu, cv, color, light, -nx, -ny, -nz);
        vertex(consumer, pose, bx, by, bz, bu, bv, color, light, -nx, -ny, -nz);
        vertex(consumer, pose, ax, ay, az, au, av, color, light, -nx, -ny, -nz);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v, int color, int light,
                               float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
