package com.artmms.starfall.client;

import com.artmms.starfall.entity.rocket.RocketEntity;

import net.minecraft.client.Minecraft;

/**
 * Small entry points that common code calls only on the client
 * (always behind a level.isClientSide() check), so client classes
 * never load on a dedicated server.
 */
public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openRocketScreen(RocketEntity rocket) {
        Minecraft.getInstance().setScreen(new RocketScreen(rocket));
    }
}
