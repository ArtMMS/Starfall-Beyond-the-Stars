package com.artmms.starfall.tag;

import com.artmms.starfall.Starfall;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** Tags used by Starfall. The JSON files live in src/main/resources/data. */
public final class ModTags {
    private ModTags() {
    }

    public static final class Fluids {
        /** Common tag shared with other mods: any crude oil works in Starfall machines. */
        public static final TagKey<Fluid> CRUDE_OIL = common("crude_oil");

        /** Fluids the basic Fluid Tank accepts (water, lava, crude oil). */
        public static final TagKey<Fluid> BASIC_TANK_FLUIDS = starfall("basic_tank_fluids");

        private Fluids() {
        }

        private static TagKey<Fluid> common(String path) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Fluid> starfall(String path) {
            return TagKey.create(Registries.FLUID, Starfall.id(path));
        }
    }
}
