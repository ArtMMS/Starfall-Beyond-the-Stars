package com.artmms.starfall.network;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.client.ClientPayloadHandler;
import com.artmms.starfall.entity.rocket.RocketEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers every Starfall packet.
 * Bump PROTOCOL_VERSION whenever a packet changes, so old clients get a clear error.
 */
@EventBusSubscriber(modid = Starfall.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class StarfallNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private StarfallNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Server -> client. The client class is only touched inside the lambda,
        // so it is never loaded on a dedicated server.
        registrar.playToClient(AlertPayload.TYPE, AlertPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPayloadHandler.handleAlert(payload)));

        // Client -> server: rocket control panel buttons
        registrar.playToServer(RocketActionPayload.TYPE, RocketActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> RocketEntity.handleAction(context.player(), payload)));
    }
}
