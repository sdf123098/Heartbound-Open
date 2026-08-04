package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record PlayCumHudAnimationS2CPacket() implements CustomPayload {
   public static final Id<PlayCumHudAnimationS2CPacket> ID = new Id<>(Identifier.of("heartbound", "play_cum_on_hud"));
   public static final PacketCodec<RegistryByteBuf, PlayCumHudAnimationS2CPacket> CODEC = PacketCodec.unit(new PlayCumHudAnimationS2CPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
