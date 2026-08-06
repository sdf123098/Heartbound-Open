package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public record RegisterCustomGirlSoundC2SPacket(String girlID, String key, SoundEvent sound) implements CustomPacketPayload {
   public static final Type<RegisterCustomGirlSoundC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "register_custom_girl_sounds"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RegisterCustomGirlSoundC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      RegisterCustomGirlSoundC2SPacket::girlID,
      ByteBufCodecs.STRING_UTF8,
      RegisterCustomGirlSoundC2SPacket::key,
      SoundEvent.DIRECT_STREAM_CODEC,
      RegisterCustomGirlSoundC2SPacket::sound,
      RegisterCustomGirlSoundC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
