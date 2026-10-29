package com.artmms.starfall.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Starfall configuration, saved as config/starfall-common.toml.
 * Chances are rolled once per event check (see eventCheckInterval).
 */
public final class StarfallConfig {
    public static final ModConfigSpec SPEC;

    // ===== Events =====
    public static final ModConfigSpec.BooleanValue ENABLE_SPACE_EVENTS;
    public static final ModConfigSpec.BooleanValue ENABLE_OVERWORLD_EVENTS;
    public static final ModConfigSpec.IntValue EVENT_CHECK_INTERVAL;
    public static final ModConfigSpec.DoubleValue METEOR_CLOUD_CHANCE;
    public static final ModConfigSpec.DoubleValue ALIEN_RAID_CHANCE;
    public static final ModConfigSpec.DoubleValue LOST_SHIP_CHANCE;
    public static final ModConfigSpec.DoubleValue FALLING_METEOR_CHANCE;
    public static final ModConfigSpec.DoubleValue ALIEN_ABDUCTION_CHANCE;
    public static final ModConfigSpec.DoubleValue CRASHING_SHIP_CHANCE;

    // ===== Consumption =====
    public static final ModConfigSpec.DoubleValue FUEL_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue ENERGY_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue OXYGEN_CONSUMPTION;

    // ===== Difficulty =====
    public static final ModConfigSpec.EnumValue<StarfallDifficulty> DIFFICULTY;
    public static final ModConfigSpec.DoubleValue MODULE_DAMAGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Random events in space and in the Overworld").push("events");
        ENABLE_SPACE_EVENTS = builder
                .comment("Enable events in space (meteor clouds, alien raids, lost ships)")
                .define("enableSpaceEvents", true);
        ENABLE_OVERWORLD_EVENTS = builder
                .comment("Enable events in the Overworld (falling meteor, abduction, crashing ship)")
                .define("enableOverworldEvents", true);
        EVENT_CHECK_INTERVAL = builder
                .comment("How often (in ticks) the game rolls for events. 20 ticks = 1 second")
                .defineInRange("eventCheckInterval", 6000, 200, 72000);
        METEOR_CLOUD_CHANCE = builder
                .comment("Chance of a meteor cloud during space travel (rare)")
                .defineInRange("meteorCloudChance", 0.05, 0.0, 1.0);
        ALIEN_RAID_CHANCE = builder
                .comment("Chance of an alien raid during space travel (rare)")
                .defineInRange("alienRaidChance", 0.03, 0.0, 1.0);
        LOST_SHIP_CHANCE = builder
                .comment("Chance of finding a lost civilization ship in space")
                .defineInRange("lostShipChance", 0.02, 0.0, 1.0);
        FALLING_METEOR_CHANCE = builder
                .comment("Chance of a meteor falling in the Overworld (extremely rare)")
                .defineInRange("fallingMeteorChance", 0.002, 0.0, 1.0);
        ALIEN_ABDUCTION_CHANCE = builder
                .comment("Chance of an alien abduction at night or during thunderstorms (rare)")
                .defineInRange("alienAbductionChance", 0.01, 0.0, 1.0);
        CRASHING_SHIP_CHANCE = builder
                .comment("Chance of an alien ship crashing in the Overworld (ultra rare)")
                .defineInRange("crashingShipChance", 0.0005, 0.0, 1.0);
        builder.pop();

        builder.comment("Resource consumption multipliers (1.0 = default)").push("consumption");
        FUEL_CONSUMPTION = builder
                .comment("Ship fuel consumption multiplier")
                .defineInRange("fuelConsumption", 1.0, 0.0, 10.0);
        ENERGY_CONSUMPTION = builder
                .comment("Ship and machine energy consumption multiplier")
                .defineInRange("energyConsumption", 1.0, 0.0, 10.0);
        OXYGEN_CONSUMPTION = builder
                .comment("Space suit oxygen consumption multiplier")
                .defineInRange("oxygenConsumption", 1.0, 0.0, 10.0);
        builder.pop();

        builder.comment("General difficulty settings").push("difficulty");
        DIFFICULTY = builder
                .comment("Overall Starfall difficulty: EASY, NORMAL or HARD")
                .defineEnum("difficulty", StarfallDifficulty.NORMAL);
        MODULE_DAMAGE = builder
                .comment("Damage multiplier applied to ship modules")
                .defineInRange("moduleDamage", 1.0, 0.0, 10.0);
        builder.pop();

        SPEC = builder.build();
    }

    private StarfallConfig() {
    }
}
