package com.artmms.starfall.client;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.entity.rocket.RocketEntity;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Client game events (key presses...). */
@EventBusSubscriber(modid = Starfall.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (StarfallKeys.ROCKET_CONTROLS.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null
                    && minecraft.player.getVehicle() instanceof RocketEntity rocket) {
                ClientHooks.openRocketScreen(rocket);
            }
        }
    }
}
