package com.cuddly.heartbound.util.variables;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum AIMode {
   WILDERNESS,
   SCENE;

   public static final StreamCodec<ByteBuf, AIMode> PACKET_CODEC = ByteBufCodecs.idMapper(i -> values()[i], Enum::ordinal);
}
