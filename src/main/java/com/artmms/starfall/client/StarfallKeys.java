package com.artmms.starfall.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

/** Starfall key bindings (changeable in Options > Controls > Key Binds). */
public final class StarfallKeys {
    public static final String CATEGORY = "key.categories.starfall";

    /** Opens the rocket control panel while seated in a rocket. */
    public static final KeyMapping ROCKET_CONTROLS = new KeyMapping(
            "key.starfall.rocket_controls", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);

    private StarfallKeys() {
    }
}
