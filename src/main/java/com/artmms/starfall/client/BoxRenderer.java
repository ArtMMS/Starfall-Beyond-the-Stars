package com.artmms.starfall.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Draws textured boxes, used to build big models (like the rocket) out of
 * block textures. Big faces are split into 1-block tiles, so textures repeat
 * like real blocks instead of stretching. Faces get the same shading as
 * Minecraft blocks (top bright, sides darker).
 */
public final class BoxRenderer {
    private static final float SHADE_UP = 1.0F;
    private static final float SHADE_DOWN = 0.5F;
    private static final float SHADE_NORTH_SOUTH = 0.8F;
    private static final float SHADE_EAST_WEST = 0.6F;

    private BoxRenderer() {
    }

    private interface Plane {
        /** Converts a point on the face (a, b) into x, y, z. */
        void vertex(VertexConsumer c, PoseStack.Pose pose, float a, float b, float u, float v, int color, int light);
    }

    /** Box in block units (0..1 = one block). */
    public static void box(VertexConsumer c, PoseStack.Pose pose, TextureAtlasSprite sprite, int light,
                           float x0, float y0, float z0, float x1, float y1, float z1) {
        box(c, pose, sprite, light, 0xFFFFFFFF, x0, y0, z0, x1, y1, z1);
    }

    public static void box(VertexConsumer c, PoseStack.Pose pose, TextureAtlasSprite sprite, int light, int color,
                           float x0, float y0, float z0, float x1, float y1, float z1) {
        // Up / Down (a = x, b = z)
        face(c, pose, sprite, light, shade(color, SHADE_UP), x0, x1, z0, z1,
                (vc, p, a, b, u, v, col, l) -> vertex(vc, p, a, y1, b, u, v, col, l, 0, 1, 0));
        face(c, pose, sprite, light, shade(color, SHADE_DOWN), x0, x1, z0, z1,
                (vc, p, a, b, u, v, col, l) -> vertex(vc, p, a, y0, b, u, v, col, l, 0, -1, 0));
        // North / South (a = x, b = y)
        face(c, pose, sprite, light, shade(color, SHADE_NORTH_SOUTH), x0, x1, y0, y1,
                (vc, p, a, b, u, v, col, l) -> vertex(vc, p, a, b, z0, u, v, col, l, 0, 0, -1));
        face(c, pose, sprite, light, shade(color, SHADE_NORTH_SOUTH), x0, x1, y0, y1,
                (vc, p, a, b, u, v, col, l) -> vertex(vc, p, a, b, z1, u, v, col, l, 0, 0, 1));
        // West / East (a = z, b = y)
        face(c, pose, sprite, light, shade(color, SHADE_EAST_WEST), z0, z1, y0, y1,
                (vc, p, a, b, u, v, col, l) -> vertex(vc, p, x0, b, a, u, v, col, l, -1, 0, 0));
        face(c, pose, sprite, light, shade(color, SHADE_EAST_WEST), z0, z1, y0, y1,
                (vc, p, a, b, u, v, col, l) -> vertex(vc, p, x1, b, a, u, v, col, l, 1, 0, 0));
    }

    /** Splits a face into tiles of at most 1x1 block and draws each on both sides. */
    private static void face(VertexConsumer c, PoseStack.Pose pose, TextureAtlasSprite sprite, int light, int color,
                             float aMin, float aMax, float bMin, float bMax, Plane plane) {
        for (float a = aMin; a < aMax - 0.0001F; a += 1.0F) {
            float a2 = Math.min(a + 1.0F, aMax);
            for (float b = bMin; b < bMax - 0.0001F; b += 1.0F) {
                float b2 = Math.min(b + 1.0F, bMax);

                float u0 = sprite.getU0();
                float u1 = u0 + (sprite.getU1() - sprite.getU0()) * (a2 - a);
                float v1 = sprite.getV1();
                float v0 = v1 - (sprite.getV1() - sprite.getV0()) * (b2 - b);

                // Front
                plane.vertex(c, pose, a, b, u0, v1, color, light);
                plane.vertex(c, pose, a, b2, u0, v0, color, light);
                plane.vertex(c, pose, a2, b2, u1, v0, color, light);
                plane.vertex(c, pose, a2, b, u1, v1, color, light);
                // Back (so the face shows from inside too, e.g. from the rocket seats)
                plane.vertex(c, pose, a2, b, u1, v1, color, light);
                plane.vertex(c, pose, a2, b2, u1, v0, color, light);
                plane.vertex(c, pose, a, b2, u0, v0, color, light);
                plane.vertex(c, pose, a, b, u0, v1, color, light);
            }
        }
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, int color, int light, float nx, float ny, float nz) {
        c.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static int shade(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.round(((color >> 16) & 0xFF) * factor);
        int g = Math.round(((color >> 8) & 0xFF) * factor);
        int b = Math.round((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
