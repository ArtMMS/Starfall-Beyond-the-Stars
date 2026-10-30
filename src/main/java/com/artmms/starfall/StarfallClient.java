package com.artmms.starfall;

import com.artmms.starfall.fluid.ModFluids;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

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
        }, ModFluids.CRUDE_OIL_TYPE.get());
    }
}
