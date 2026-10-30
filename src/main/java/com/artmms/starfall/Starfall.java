package com.artmms.starfall;

import org.slf4j.Logger;

import com.artmms.starfall.block.ModBlocks;
import com.artmms.starfall.block.entity.ModBlockEntities;
import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.entity.ModEntities;
import com.artmms.starfall.fluid.ModFluids;
import com.artmms.starfall.item.ModCreativeTabs;
import com.artmms.starfall.item.ModItems;
import com.artmms.starfall.menu.ModMenus;
import com.artmms.starfall.sound.ModSounds;
import com.artmms.starfall.world.ModFeatures;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
        // Blocks first: they also register their block items into ModItems.ITEMS
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
        ModFluids.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModFeatures.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, StarfallConfig.SPEC);

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
