package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformThrustKeyframeC2SPacket() implements CustomPacketPayload {
   public static final Type<TransformThrustKeyframeC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_thrust_keyframe"));
   public static final StreamCodec<ByteBuf, TransformThrustKeyframeC2SPacket> CODEC = StreamCodec.unit(new TransformThrustKeyframeC2SPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
