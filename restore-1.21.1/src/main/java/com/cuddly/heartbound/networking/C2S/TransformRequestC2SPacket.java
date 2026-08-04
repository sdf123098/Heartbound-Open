package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformRequestC2SPacket(String girlId) implements CustomPayload {
   public static final Id<TransformRequestC2SPacket> ID = new Id<>(Identifier.of("heartbound", "transform_request"));
   public static final PacketCodec<RegistryByteBuf, TransformRequestC2SPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.STRING, TransformRequestC2SPacket::girlId, TransformRequestC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
