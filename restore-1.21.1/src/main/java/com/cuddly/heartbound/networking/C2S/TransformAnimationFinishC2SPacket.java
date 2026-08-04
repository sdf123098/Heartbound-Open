package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformAnimationFinishC2SPacket() implements CustomPayload {
   public static final Id<TransformAnimationFinishC2SPacket> ID = new Id<>(Identifier.of("heartbound", "transform_anim_finish"));
   public static final PacketCodec<ByteBuf, TransformAnimationFinishC2SPacket> CODEC = PacketCodec.unit(new TransformAnimationFinishC2SPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
