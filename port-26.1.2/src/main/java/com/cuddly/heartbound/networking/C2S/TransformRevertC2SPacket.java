package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformRevertC2SPacket() implements CustomPacketPayload {
   public static final Type<TransformRevertC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_revert"));
   public static final StreamCodec<RegistryFriendlyByteBuf, TransformRevertC2SPacket> CODEC = StreamCodec.unit(new TransformRevertC2SPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
