package com.artmms.starfall.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Starfall configuration, saved as config/starfall-common.toml.
 * Also editable in game: Mods > Starfall > Config.
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
    public static final ModConfigSpec.DoubleValue OXYGEN_CONSUMPTION;

    // ===== Fuel (fuel points per 1000 mB / 1 bucket) =====
    public static final ModConfigSpec.IntValue CRUDE_OIL_FUEL_POINTS;
    public static final ModConfigSpec.IntValue LAVA_FUEL_POINTS;
    public static final ModConfigSpec.IntValue COAL_FUEL_POINTS;

    // ===== Rocket =====
    public static final ModConfigSpec.IntValue ROCKET_TANK_BASIC;
    public static final ModConfigSpec.IntValue ROCKET_TANK_REINFORCED;
    public static final ModConfigSpec.IntValue ROCKET_TANK_INDUSTRIAL;
    public static final ModConfigSpec.IntValue ROCKET_LAUNCH_COST;
    public static final ModConfigSpec.DoubleValue ROCKET_SPEED;
    public static final ModConfigSpec.DoubleValue ROCKET_TRIPLE_ENGINE_SPEED;
    public static final ModConfigSpec.IntValue ROCKET_TEST_FLIGHT_SECONDS;

    // ===== Machines =====
    public static final ModConfigSpec.IntValue BASIC_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue CRANK_PUMP_AMOUNT;
    public static final ModConfigSpec.IntValue EXTRACTOR_RANGE;

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
                .comment("Rocket fuel consumption multiplier")
                .defineInRange("fuelConsumption", 1.0, 0.0, 10.0);
        OXYGEN_CONSUMPTION = builder
                .comment("Space suit oxygen consumption multiplier")
                .defineInRange("oxygenConsumption", 1.0, 0.0, 10.0);
        builder.pop();

        builder.comment("Fuel Points (FP) each fuel gives per bucket (1000 mB)").push("fuel");
        CRUDE_OIL_FUEL_POINTS = builder
                .comment("Fuel Points per bucket of crude oil (the best fuel)")
                .defineInRange("crudeOilFuelPoints", 100, 0, 100000);
        LAVA_FUEL_POINTS = builder
                .comment("Fuel Points per bucket of lava")
                .defineInRange("lavaFuelPoints", 60, 0, 100000);
        COAL_FUEL_POINTS = builder
                .comment("Fuel Points per coal or charcoal (a block of coal gives 9x this)")
                .defineInRange("coalFuelPoints", 15, 0, 100000);
        builder.pop();

        builder.comment("Rocket").push("rocket");
        ROCKET_TANK_BASIC = builder
                .comment("Fuel tank capacity (FP) of the Basic tank")
                .defineInRange("tankBasic", 1000, 1, 1000000);
        ROCKET_TANK_REINFORCED = builder
                .comment("Fuel tank capacity (FP) of the Reinforced tank")
                .defineInRange("tankReinforced", 2500, 1, 1000000);
        ROCKET_TANK_INDUSTRIAL = builder
                .comment("Fuel tank capacity (FP) of the Industrial tank")
                .defineInRange("tankIndustrial", 5000, 1, 1000000);
        ROCKET_LAUNCH_COST = builder
                .comment("Fuel Points spent on each launch (multiplied by fuelConsumption)")
                .defineInRange("launchCost", 100, 0, 1000000);
        ROCKET_SPEED = builder
                .comment("Climb speed with 1 engine, in blocks per tick")
                .defineInRange("speed", 0.25, 0.01, 5.0);
        ROCKET_TRIPLE_ENGINE_SPEED = builder
                .comment("Speed multiplier with 3 engines (same fuel cost)")
                .defineInRange("tripleEngineSpeed", 2.5, 1.0, 10.0);
        ROCKET_TEST_FLIGHT_SECONDS = builder
                .comment("How long the test flight climbs before coming back down, in seconds")
                .defineInRange("testFlightSeconds", 8, 1, 120);
        builder.pop();

        builder.comment("Machines and storage").push("machines");
        BASIC_TANK_CAPACITY = builder
                .comment("Capacity of the Fluid Tank, in mB (1000 mB = 1 bucket)")
                .defineInRange("basicTankCapacity", 16000, 1000, 1000000);
        CRANK_PUMP_AMOUNT = builder
                .comment("How much each Steel Crank turn pumps into every connected tank, in mB. Whole buckets only (1000 = 1 bucket)")
                .defineInRange("crankPumpAmount", 1000, 1000, 100000);
        EXTRACTOR_RANGE = builder
                .comment("How far (in blocks) the Oil Extractor reaches into a connected pocket or pool")
                .defineInRange("extractorRange", 8, 1, 32);
        builder.pop();

        builder.comment("General difficulty settings").push("difficulty");
        DIFFICULTY = builder
                .comment("Overall Starfall difficulty: EASY, NORMAL or HARD")
                .defineEnum("difficulty", StarfallDifficulty.NORMAL);
        MODULE_DAMAGE = builder
                .comment("Damage multiplier applied to rocket parts")
                .defineInRange("moduleDamage", 1.0, 0.0, 10.0);
        builder.pop();

        SPEC = builder.build();
    }

    private StarfallConfig() {
    }
}
