package com.artmms.starfall.world;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.world.feature.OilPocketFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * World generation features. Where and how often they spawn is defined in JSON:
 * data/starfall/worldgen/placed_feature and data/starfall/neoforge/biome_modifier.
 */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Starfall.MODID);

    public static final DeferredHolder<Feature<?>, OilPocketFeature> OIL_POCKET =
            FEATURES.register("oil_pocket", OilPocketFeature::new);

    private ModFeatures() {
    }

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
