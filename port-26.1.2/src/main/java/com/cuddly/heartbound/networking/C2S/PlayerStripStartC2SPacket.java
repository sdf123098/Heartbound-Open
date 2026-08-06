package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayerStripStartC2SPacket() implements CustomPacketPayload {
   public static final Type<PlayerStripStartC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "player_strip_start"));
   public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStripStartC2SPacket> CODEC = StreamCodec.unit(new PlayerStripStartC2SPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
