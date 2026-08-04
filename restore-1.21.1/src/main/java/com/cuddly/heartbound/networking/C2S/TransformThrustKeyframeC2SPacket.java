package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformThrustKeyframeC2SPacket() implements CustomPayload {
   public static final Id<TransformThrustKeyframeC2SPacket> ID = new Id<>(Identifier.of("heartbound", "transform_thrust_keyframe"));
   public static final PacketCodec<ByteBuf, TransformThrustKeyframeC2SPacket> CODEC = PacketCodec.unit(new TransformThrustKeyframeC2SPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
