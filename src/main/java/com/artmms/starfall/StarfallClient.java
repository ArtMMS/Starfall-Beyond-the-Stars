package com.artmms.starfall;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only entry point. This class never loads on a dedicated server,
 * so client code (rendering, HUD, keybinds) is safe here.
 */
@Mod(value = Starfall.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Starfall.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StarfallClient {
    public StarfallClient(ModContainer container) {
        // Config screen: Mods > Starfall > Config
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Starfall.LOGGER.info("Starfall client setup complete.");
    }
}
