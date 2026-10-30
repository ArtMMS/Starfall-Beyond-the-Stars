package com.artmms.starfall.entity.rocket;

/**
 * The parts of the rocket. Each one will have its own health (HP), shown on the
 * pilot panel; a destroyed part disables what it does. The HP values are already
 * saved with the rocket, damage will be added later (meteors, aliens...).
 */
public enum RocketPart {
    NOSE("nose", 100),
    CAPSULE("capsule", 150),
    FUEL_TANK("fuel_tank", 200),
    ENGINE_CENTER("engine_center", 120),
    ENGINE_LEFT("engine_left", 120),
    ENGINE_RIGHT("engine_right", 120);

    private final String name;
    private final int maxHealth;

    RocketPart(String name, int maxHealth) {
        this.name = name;
        this.maxHealth = maxHealth;
    }

    public String id() {
        return name;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public String translationKey() {
        return "rocket_part.starfall." + name;
    }

    /** Side engines only exist on rockets with 3 engines. */
    public boolean isInstalled(int engines) {
        return (this != ENGINE_LEFT && this != ENGINE_RIGHT) || engines >= 3;
    }
}
