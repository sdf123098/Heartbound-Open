package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RegisterCustomGirlMessageC2SPacket(String girlID, String key, String message) implements CustomPacketPayload {
   public static final Type<RegisterCustomGirlMessageC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "register_custom_girl_messages"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RegisterCustomGirlMessageC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      RegisterCustomGirlMessageC2SPacket::girlID,
      ByteBufCodecs.STRING_UTF8,
      RegisterCustomGirlMessageC2SPacket::key,
      ByteBufCodecs.STRING_UTF8,
      RegisterCustomGirlMessageC2SPacket::message,
      RegisterCustomGirlMessageC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
