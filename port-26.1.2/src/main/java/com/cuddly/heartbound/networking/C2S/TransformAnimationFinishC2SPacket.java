package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformAnimationFinishC2SPacket() implements CustomPacketPayload {
   public static final Type<TransformAnimationFinishC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_anim_finish"));
   public static final StreamCodec<ByteBuf, TransformAnimationFinishC2SPacket> CODEC = StreamCodec.unit(new TransformAnimationFinishC2SPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
