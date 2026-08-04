package com.cuddly.heartbound.util.variables;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum SceneType {
   ON_PLAYER,
   ON_BED,
   STATIONARY_CONTACT,
   STATIONARY_INTRO,
   STATIONARY;

   public static final PacketCodec<ByteBuf, SceneType> PACKET_CODEC = PacketCodecs.indexed(i -> values()[i], Enum::ordinal);
   public static final Codec<SceneType> CODEC = Codec.INT.xmap(i -> values()[i], Enum::ordinal);
}
