package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RefreshModelsS2CPacket() implements CustomPacketPayload {
   public static final Type<RefreshModelsS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "refresh_models"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RefreshModelsS2CPacket> CODEC = StreamCodec.unit(new RefreshModelsS2CPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
