package com.artmms.starfall.block.entity;

import com.artmms.starfall.Starfall;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Exposes Starfall tanks as standard NeoForge fluid handlers, so buckets,
 * pipes and other mods can interact with them.
 */
@EventBusSubscriber(modid = Starfall.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FLUID_TANK.get(),
                (tank, side) -> tank.getTank());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.OIL_EXTRACTOR.get(),
                (extractor, side) -> extractor.getTank());
    }
}
