package com.artmms.starfall.entity.rocket;

import com.artmms.starfall.config.StarfallConfig;

import net.minecraft.resources.ResourceLocation;

/**
 * Rocket fuel tank levels. Each one holds more Fuel Points.
 * For now they are switched from the rocket panel; later they will be real upgrades.
 */
public enum RocketTankTier {
    BASIC("basic", "minecraft:block/iron_block"),
    REINFORCED("reinforced", "minecraft:block/copper_block"),
    INDUSTRIAL("industrial", "minecraft:block/gold_block");

    private final String name;
    /** Texture of the colored band around the tank section (shows the tier). */
    private final ResourceLocation bandTexture;

    RocketTankTier(String name, String bandTexture) {
        this.name = name;
        this.bandTexture = ResourceLocation.parse(bandTexture);
    }

    public int capacity() {
        return switch (this) {
            case BASIC -> StarfallConfig.ROCKET_TANK_BASIC.get();
            case REINFORCED -> StarfallConfig.ROCKET_TANK_REINFORCED.get();
            case INDUSTRIAL -> StarfallConfig.ROCKET_TANK_INDUSTRIAL.get();
        };
    }

    public String translationKey() {
        return "rocket_tier.starfall." + name;
    }

    public ResourceLocation bandTexture() {
        return bandTexture;
    }

    public RocketTankTier next() {
        RocketTankTier[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static RocketTankTier byId(int id) {
        RocketTankTier[] values = values();
        return id >= 0 && id < values.length ? values[id] : BASIC;
    }
}
