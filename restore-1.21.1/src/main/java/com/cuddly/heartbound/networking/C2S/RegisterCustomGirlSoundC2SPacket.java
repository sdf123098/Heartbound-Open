package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record RegisterCustomGirlSoundC2SPacket(String girlID, String key, SoundEvent sound) implements CustomPayload {
   public static final Id<RegisterCustomGirlSoundC2SPacket> ID = new Id<>(Identifier.of("heartbound", "register_custom_girl_sounds"));
   public static final PacketCodec<RegistryByteBuf, RegisterCustomGirlSoundC2SPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.STRING,
      RegisterCustomGirlSoundC2SPacket::girlID,
      PacketCodecs.STRING,
      RegisterCustomGirlSoundC2SPacket::key,
      SoundEvent.PACKET_CODEC,
      RegisterCustomGirlSoundC2SPacket::sound,
      RegisterCustomGirlSoundC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
