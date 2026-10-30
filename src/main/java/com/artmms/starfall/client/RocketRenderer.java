package com.artmms.starfall.client;

import com.artmms.starfall.entity.rocket.RocketEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Draws the rocket, about 19 blocks tall, built from Minecraft block textures:
 * - Fins and engines at the bottom (1 or 3 engines)
 * - Fuel tank section with a band that shows the tank level
 * - Hollow crew capsule with two windows (co-pilot below, pilot above)
 * - Pointed nose
 * - Flames under the engines while flying
 *
 * Coordinates are in blocks, centered on the rocket (x/z from -1.5 to 1.5 for the body).
 * The windows face south (+z) before the rocket's rotation is applied.
 */
public class RocketRenderer extends EntityRenderer<RocketEntity> {
    private static final ResourceLocation HULL = tex("white_concrete");
    private static final ResourceLocation CAPSULE = tex("light_gray_concrete");
    private static final ResourceLocation ACCENT = tex("red_nether_bricks");
    private static final ResourceLocation GLASS = tex("glass");
    private static final ResourceLocation ENGINE = tex("iron_block");
    private static final ResourceLocation NOZZLE = tex("polished_blackstone");
    private static final ResourceLocation FIRE = tex("fire_0");

    // Body
    private static final float R = 1.5F;          // body half width
    private static final float ENGINE_TOP = 1.2F; // where the body starts
    private static final float TANK_TOP = 9.0F;   // tank section -> capsule
    private static final float CAPSULE_TOP = 15.5F;
    private static final float WALL = 0.1F;       // capsule wall thickness
    // Windows (x from -WIN to WIN)
    private static final float WIN = 0.8F;
    private static final float LOW_WINDOW_BOTTOM = 10.8F;
    private static final float LOW_WINDOW_TOP = 12.2F;
    private static final float HIGH_WINDOW_BOTTOM = 13.6F;
    private static final float HIGH_WINDOW_TOP = 15.0F;

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.8F;
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.withDefaultNamespace("block/" + name);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity rocket) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(RocketEntity rocket, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-rocket.getYRot()));

        VertexConsumer solid = buffer.getBuffer(RenderType.solid());
        VertexConsumer cutout = buffer.getBuffer(RenderType.cutout());
        PoseStack.Pose pose = poseStack.last();
        int light = packedLight;

        // ===== Fins =====
        TextureAtlasSprite accent = sprite(ACCENT);
        BoxRenderer.box(solid, pose, accent, light, -2.5F, 0.0F, -0.2F, -R, 3.2F, 0.2F);
        BoxRenderer.box(solid, pose, accent, light, R, 0.0F, -0.2F, 2.5F, 3.2F, 0.2F);
        BoxRenderer.box(solid, pose, accent, light, -0.2F, 0.0F, -2.5F, 0.2F, 3.2F, -R);
        BoxRenderer.box(solid, pose, accent, light, -0.2F, 0.0F, R, 0.2F, 3.2F, 2.5F);

        // ===== Engines =====
        drawEngine(solid, pose, light, 0.0F, 0.5F);
        if (rocket.getEngines() >= 3) {
            drawEngine(solid, pose, light, -1.0F, 0.38F);
            drawEngine(solid, pose, light, 1.0F, 0.38F);
        }

        // ===== Fuel tank section =====
        TextureAtlasSprite hull = sprite(HULL);
        BoxRenderer.box(solid, pose, hull, light, -R, ENGINE_TOP, -R, R, TANK_TOP, R);
        BoxRenderer.box(solid, pose, accent, light, -R - 0.05F, ENGINE_TOP, -R - 0.05F, R + 0.05F, 2.0F, R + 0.05F);
        // The band shows the tank level (iron / copper / gold)
        BoxRenderer.box(solid, pose, sprite(rocket.getTankTier().bandTexture()), light,
                -R - 0.05F, 5.0F, -R - 0.05F, R + 0.05F, 6.0F, R + 0.05F);

        // ===== Crew capsule (hollow, with two windows on the front and back) =====
        drawCapsule(solid, cutout, pose, light);

        // ===== Nose =====
        BoxRenderer.box(solid, pose, hull, light, -1.2F, CAPSULE_TOP, -1.2F, 1.2F, 16.6F, 1.2F);
        BoxRenderer.box(solid, pose, hull, light, -0.8F, 16.6F, -0.8F, 0.8F, 17.5F, 0.8F);
        BoxRenderer.box(solid, pose, accent, light, -0.4F, 17.5F, -0.4F, 0.4F, 18.4F, 0.4F);

        // ===== Flames =====
        if (rocket.isFlying()) {
            float time = rocket.tickCount + partialTick;
            drawFlame(cutout, pose, 0.0F, 0.45F, time);
            if (rocket.getEngines() >= 3) {
                drawFlame(cutout, pose, -1.0F, 0.32F, time + 3.0F);
                drawFlame(cutout, pose, 1.0F, 0.32F, time + 7.0F);
            }
        }

        poseStack.popPose();
        super.render(rocket, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void drawEngine(VertexConsumer c, PoseStack.Pose pose, int light, float x, float half) {
        // Bell
        BoxRenderer.box(c, pose, sprite(ENGINE), light, x - half, 0.3F, -half, x + half, ENGINE_TOP, half);
        // Nozzle, a bit wider
        float nozzle = half + 0.12F;
        BoxRenderer.box(c, pose, sprite(NOZZLE), light, x - nozzle, 0.0F, -nozzle, x + nozzle, 0.3F, nozzle);
    }

    private void drawFlame(VertexConsumer c, PoseStack.Pose pose, float x, float half, float time) {
        // Flickers between 1.2 and 2 blocks long
        float length = 1.6F + (float) Math.sin(time * 1.7F) * 0.4F;
        BoxRenderer.box(c, pose, sprite(FIRE), LightTexture.FULL_BRIGHT,
                x - half, -length, -half, x + half, 0.0F, half);
    }

    private void drawCapsule(VertexConsumer solid, VertexConsumer cutout, PoseStack.Pose pose, int light) {
        TextureAtlasSprite capsule = sprite(CAPSULE);
        TextureAtlasSprite accent = sprite(ACCENT);
        float y0 = TANK_TOP;
        float y1 = CAPSULE_TOP;

        // Side walls (full)
        BoxRenderer.box(solid, pose, capsule, light, -R, y0, -R, -R + WALL, y1, R);
        BoxRenderer.box(solid, pose, capsule, light, R - WALL, y0, -R, R, y1, R);

        // Front and back walls, with holes for the windows
        for (int side = 0; side < 2; side++) {
            float z0 = side == 0 ? R - WALL : -R;
            float z1 = side == 0 ? R : -R + WALL;
            // Columns beside the windows
            BoxRenderer.box(solid, pose, capsule, light, -R + WALL, y0, z0, -WIN, y1, z1);
            BoxRenderer.box(solid, pose, capsule, light, WIN, y0, z0, R - WALL, y1, z1);
            // Below, between and above the windows
            BoxRenderer.box(solid, pose, capsule, light, -WIN, y0, z0, WIN, LOW_WINDOW_BOTTOM, z1);
            BoxRenderer.box(solid, pose, capsule, light, -WIN, LOW_WINDOW_TOP, z0, WIN, HIGH_WINDOW_BOTTOM, z1);
            BoxRenderer.box(solid, pose, capsule, light, -WIN, HIGH_WINDOW_TOP, z0, WIN, y1, z1);
            // Glass panes
            float gz0 = z0 + WALL * 0.4F;
            float gz1 = z0 + WALL * 0.6F;
            BoxRenderer.box(cutout, pose, sprite(GLASS), light, -WIN, LOW_WINDOW_BOTTOM, gz0, WIN, LOW_WINDOW_TOP, gz1);
            BoxRenderer.box(cutout, pose, sprite(GLASS), light, -WIN, HIGH_WINDOW_BOTTOM, gz0, WIN, HIGH_WINDOW_TOP, gz1);
        }

        // Red stripe where the capsule starts
        BoxRenderer.box(solid, pose, accent, light, -R - 0.05F, y0, -R - 0.05F, R + 0.05F, y0 + 0.4F, R + 0.05F);
    }

    private static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }
}
