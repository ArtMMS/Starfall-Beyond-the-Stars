package com.artmms.starfall.network;

import com.artmms.starfall.Starfall;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> server: a button pressed on the rocket control panel.
 *
 * @param entityId the rocket
 * @param action   one of the constants below
 */
public record RocketActionPayload(int entityId, int action) implements CustomPacketPayload {
    public static final int LAUNCH = 0;
    public static final int CYCLE_TANK = 1;
    public static final int TOGGLE_ENGINES = 2;

    public static final Type<RocketActionPayload> TYPE = new Type<>(Starfall.id("rocket_action"));

    public static final StreamCodec<ByteBuf, RocketActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RocketActionPayload::entityId,
            ByteBufCodecs.VAR_INT, RocketActionPayload::action,
            RocketActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
