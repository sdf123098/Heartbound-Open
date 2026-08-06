package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayCumHudAnimationS2CPacket() implements CustomPacketPayload {
   public static final Type<PlayCumHudAnimationS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "play_cum_on_hud"));
   public static final StreamCodec<RegistryFriendlyByteBuf, PlayCumHudAnimationS2CPacket> CODEC = StreamCodec.unit(new PlayCumHudAnimationS2CPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
