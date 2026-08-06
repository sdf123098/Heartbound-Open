package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformStopSceneC2SPacket() implements CustomPacketPayload {
   public static final Type<TransformStopSceneC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_stop_scene"));
   public static final StreamCodec<ByteBuf, TransformStopSceneC2SPacket> CODEC = StreamCodec.unit(new TransformStopSceneC2SPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
