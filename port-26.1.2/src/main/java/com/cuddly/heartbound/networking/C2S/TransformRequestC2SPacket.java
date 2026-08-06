package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformRequestC2SPacket(String girlId) implements CustomPacketPayload {
   public static final Type<TransformRequestC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_request"));
   public static final StreamCodec<RegistryFriendlyByteBuf, TransformRequestC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, TransformRequestC2SPacket::girlId, TransformRequestC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
