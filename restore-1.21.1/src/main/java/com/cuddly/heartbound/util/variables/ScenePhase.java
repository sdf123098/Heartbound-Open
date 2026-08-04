package com.cuddly.heartbound.util.variables;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum ScenePhase {
   NONE,
   DIALOG,
   LAYING_DOWN,
   BED_IDLE,
   INTRO,
   MOVING,
   HAVING_SEX,
   CUM,
   STATIONARY_INTRO,
   STATIONARY;

   public static final PacketCodec<ByteBuf, ScenePhase> PACKET_CODEC = PacketCodecs.indexed(i -> values()[i], Enum::ordinal);
   public static final Codec<ScenePhase> CODEC = Codec.INT.xmap(i -> values()[i], Enum::ordinal);
}
