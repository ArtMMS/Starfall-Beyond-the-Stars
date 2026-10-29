package com.artmms.starfall.network;

import com.artmms.starfall.Starfall;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> client: shows an alert to the player.
 * Will be used by the onboard AI, the atmosphere panel and events.
 *
 * @param translationKey lang key of the message
 * @param level          alert priority (changes the color)
 */
public record AlertPayload(String translationKey, AlertLevel level) implements CustomPacketPayload {
    public static final Type<AlertPayload> TYPE = new Type<>(Starfall.id("alert"));

    public static final StreamCodec<ByteBuf, AlertPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AlertPayload::translationKey,
            AlertLevel.STREAM_CODEC, AlertPayload::level,
            AlertPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
