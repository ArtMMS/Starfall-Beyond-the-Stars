package com.artmms.starfall;

import org.joml.Vector3f;

import com.artmms.starfall.block.entity.ModBlockEntities;
import com.artmms.starfall.client.FluidTankRenderer;
import com.artmms.starfall.client.RocketRenderer;
import com.artmms.starfall.client.StarfallKeys;
import com.artmms.starfall.client.SteelCrankRenderer;
import com.artmms.starfall.entity.ModEntities;
import com.artmms.starfall.fluid.ModFluids;
import com.artmms.starfall.item.ModItems;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

/**
 * Client-only entry point. This class never loads on a dedicated server,
 * so client code (rendering, HUD, keybinds) is safe here.
 */
@Mod(value = Starfall.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Starfall.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StarfallClient {
    /** Crude oil uses the vanilla water textures, tinted almost black. */
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final int CRUDE_OIL_COLOR = 0xFF17130F;

    /** Fog when the camera is inside crude oil: almost black and very close, like lava. */
    private static final Vector3f CRUDE_OIL_FOG_COLOR = new Vector3f(0.02F, 0.015F, 0.01F);
    private static final float CRUDE_OIL_FOG_START = 0.0F;
    private static final float CRUDE_OIL_FOG_END = 1.5F;

    public StarfallClient(ModContainer container) {
        // Config screen: Mods > Starfall > Config
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Starfall.LOGGER.info("Starfall client setup complete.");
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public int getTintColor() {
                return CRUDE_OIL_COLOR;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                           int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return CRUDE_OIL_FOG_COLOR;
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance,
                                        float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(CRUDE_OIL_FOG_START);
                RenderSystem.setShaderFogEnd(CRUDE_OIL_FOG_END);
            }
        }, ModFluids.CRUDE_OIL_TYPE.get());
    }

    /** Draws the liquid inside the Fluid Tank glass, the crank and the rocket. */
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STEEL_CRANK.get(), SteelCrankRenderer::new);
        event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
    }

    /** R (by default): opens the rocket panel while seated. */
    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(StarfallKeys.ROCKET_CONTROLS);
    }

    /** Paints the liquid inside the vanilla bucket model with the fluid's color (black for crude oil). */
    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new DynamicFluidContainerModel.Colors(), ModItems.CRUDE_OIL_BUCKET.get());
    }
}
