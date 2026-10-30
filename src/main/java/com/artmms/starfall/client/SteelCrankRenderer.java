package com.artmms.starfall.client;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.block.SteelCrankBlock;
import com.artmms.starfall.block.entity.SteelCrankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Draws the Steel Crank and spins it smoothly around the axis of the pipe it's attached to.
 *
 * The crank is modeled attached to the floor (pipe below, y = 0) and then rotated
 * to face whichever side its pipe is on.
 */
public class SteelCrankRenderer implements BlockEntityRenderer<SteelCrankBlockEntity> {
    private static final ResourceLocation TEXTURE = Starfall.id("block/steel_crank");
    private static final int WHITE = 0xFFFFFFFF;

    /** Boxes in pixels (0..16): {minX, minY, minZ, maxX, maxY, maxZ}. */
    private static final float[][] BOXES = {
            {6, 0, 6, 10, 4, 10},   // connector: same thickness as the pipe, so it continues it
            {5, 4, 5, 11, 5, 11},   // hub plate
            {7, 5, 7, 9, 10, 9},    // shaft
            {7, 8, 7, 14, 10, 9},   // arm
            {12, 10, 7, 14, 15, 9}  // handle
    };

    public SteelCrankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SteelCrankBlockEntity crank, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction attached = crank.getBlockState().getValue(SteelCrankBlock.FACING);
        float progress = crank.getSpinProgress(partialTick);
        // Ease in and out, so the turn starts and stops softly
        float eased = progress * progress * (3.0F - 2.0F * progress);
        float angle = eased * 360.0F;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        // Model "up" points away from the pipe
        poseStack.mulPose(attached.getOpposite().getRotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        PoseStack.Pose pose = poseStack.last();
        for (float[] box : BOXES) {
            drawBox(consumer, pose, sprite, packedLight,
                    box[0] / 16F, box[1] / 16F, box[2] / 16F,
                    box[3] / 16F, box[4] / 16F, box[5] / 16F);
        }
        poseStack.popPose();
    }

    private static void drawBox(VertexConsumer c, PoseStack.Pose pose, TextureAtlasSprite sprite, int light,
                                float x0, float y0, float z0, float x1, float y1, float z1) {
        // UVs follow the box size, like a normal block model
        float uA = u(sprite, x0);
        float uB = u(sprite, x1);
        float uC = u(sprite, z0);
        float uD = u(sprite, z1);
        float vTop = v(sprite, 1.0F - y1);
        float vBottom = v(sprite, 1.0F - y0);
        float vZ0 = v(sprite, z0);
        float vZ1 = v(sprite, z1);

        // Up / Down
        quad(c, pose, light, 0, 1, 0,
                x0, y1, z0, uA, vZ0, x0, y1, z1, uA, vZ1, x1, y1, z1, uB, vZ1, x1, y1, z0, uB, vZ0);
        quad(c, pose, light, 0, -1, 0,
                x0, y0, z1, uA, vZ1, x0, y0, z0, uA, vZ0, x1, y0, z0, uB, vZ0, x1, y0, z1, uB, vZ1);
        // North / South
        quad(c, pose, light, 0, 0, -1,
                x1, y1, z0, uB, vTop, x1, y0, z0, uB, vBottom, x0, y0, z0, uA, vBottom, x0, y1, z0, uA, vTop);
        quad(c, pose, light, 0, 0, 1,
                x0, y1, z1, uA, vTop, x0, y0, z1, uA, vBottom, x1, y0, z1, uB, vBottom, x1, y1, z1, uB, vTop);
        // West / East
        quad(c, pose, light, -1, 0, 0,
                x0, y1, z0, uC, vTop, x0, y0, z0, uC, vBottom, x0, y0, z1, uD, vBottom, x0, y1, z1, uD, vTop);
        quad(c, pose, light, 1, 0, 0,
                x1, y1, z1, uD, vTop, x1, y0, z1, uD, vBottom, x1, y0, z0, uC, vBottom, x1, y1, z0, uC, vTop);
    }

    private static float u(TextureAtlasSprite sprite, float fraction) {
        return sprite.getU0() + (sprite.getU1() - sprite.getU0()) * fraction;
    }

    private static float v(TextureAtlasSprite sprite, float fraction) {
        return sprite.getV0() + (sprite.getV1() - sprite.getV0()) * fraction;
    }

    /** Draws a quad on both sides, so the winding order never hides a face. */
    private static void quad(VertexConsumer c, PoseStack.Pose pose, int light, float nx, float ny, float nz,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv) {
        vertex(c, pose, ax, ay, az, au, av, light, nx, ny, nz);
        vertex(c, pose, bx, by, bz, bu, bv, light, nx, ny, nz);
        vertex(c, pose, cx, cy, cz, cu, cv, light, nx, ny, nz);
        vertex(c, pose, dx, dy, dz, du, dv, light, nx, ny, nz);

        vertex(c, pose, dx, dy, dz, du, dv, light, -nx, -ny, -nz);
        vertex(c, pose, cx, cy, cz, cu, cv, light, -nx, -ny, -nz);
        vertex(c, pose, bx, by, bz, bu, bv, light, -nx, -ny, -nz);
        vertex(c, pose, ax, ay, az, au, av, light, -nx, -ny, -nz);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, int light, float nx, float ny, float nz) {
        c.addVertex(pose, x, y, z)
                .setColor(WHITE)
                .setUv(u, v)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
