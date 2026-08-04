package com.cuddly.heartbound.util.variables;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum AIMode {
   WILDERNESS,
   SCENE;

   public static final PacketCodec<ByteBuf, AIMode> PACKET_CODEC = PacketCodecs.indexed(i -> values()[i], Enum::ordinal);
}
