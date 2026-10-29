package com.artmms.starfall;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Main class of Starfall: Beyond the Stars.
 * The value in @Mod must match mod_id in gradle.properties.
 */
@Mod(Starfall.MODID)
public class Starfall {
    public static final String MODID = "starfall";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Starfall(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Starfall: Beyond the Stars is loading...");
    }

    /** Helper to create ids in the mod namespace, e.g. starfall:fuel. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
