package com.artmms.starfall.client;

import com.artmms.starfall.network.AlertPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Client-side handling of packets sent by the server. Only runs on the client. */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleAlert(AlertPayload payload) {
        Component message = Component.translatable(payload.translationKey())
                .withStyle(payload.level().color());

        // Shows above the hotbar (action bar)
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }
}
