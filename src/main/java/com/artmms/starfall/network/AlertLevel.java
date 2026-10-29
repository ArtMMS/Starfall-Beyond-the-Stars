package com.artmms.starfall.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Alert priority used by the onboard AI and detection machines. */
public enum AlertLevel {
    INFO(ChatFormatting.AQUA),
    WARNING(ChatFormatting.GOLD),
    CRITICAL(ChatFormatting.RED);

    public static final StreamCodec<ByteBuf, AlertLevel> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(AlertLevel::byId, AlertLevel::ordinal);

    private final ChatFormatting color;

    AlertLevel(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting color() {
        return color;
    }

    public static AlertLevel byId(int id) {
        AlertLevel[] values = values();
        return id >= 0 && id < values.length ? values[id] : INFO;
    }
}
