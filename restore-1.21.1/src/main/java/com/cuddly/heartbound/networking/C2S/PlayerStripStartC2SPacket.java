package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record PlayerStripStartC2SPacket() implements CustomPayload {
   public static final Id<PlayerStripStartC2SPacket> ID = new Id<>(Identifier.of("heartbound", "player_strip_start"));
   public static final PacketCodec<RegistryByteBuf, PlayerStripStartC2SPacket> CODEC = PacketCodec.unit(new PlayerStripStartC2SPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
