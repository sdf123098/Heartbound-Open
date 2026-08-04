package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record PlayerStripToggleC2SPacket() implements CustomPayload {
   public static final Id<PlayerStripToggleC2SPacket> ID = new Id<>(Identifier.of("heartbound", "player_strip_toggle"));
   public static final PacketCodec<RegistryByteBuf, PlayerStripToggleC2SPacket> CODEC = PacketCodec.unit(new PlayerStripToggleC2SPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
