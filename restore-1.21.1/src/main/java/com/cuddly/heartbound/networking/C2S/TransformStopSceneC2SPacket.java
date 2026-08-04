package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformStopSceneC2SPacket() implements CustomPayload {
   public static final Id<TransformStopSceneC2SPacket> ID = new Id<>(Identifier.of("heartbound", "transform_stop_scene"));
   public static final PacketCodec<ByteBuf, TransformStopSceneC2SPacket> CODEC = PacketCodec.unit(new TransformStopSceneC2SPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
