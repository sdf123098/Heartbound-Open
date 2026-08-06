package com.cuddly.heartbound.networking.C2S;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public record RegisterCustomGirlRandomSoundC2SPacket(String girlID, String key, List<SoundEvent> sounds) implements CustomPacketPayload {
   public static final Type<RegisterCustomGirlRandomSoundC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "register_custom_girl_random_sounds"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RegisterCustomGirlRandomSoundC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      RegisterCustomGirlRandomSoundC2SPacket::girlID,
      ByteBufCodecs.STRING_UTF8,
      RegisterCustomGirlRandomSoundC2SPacket::key,
      ByteBufCodecs.collection(ArrayList::new, SoundEvent.DIRECT_STREAM_CODEC),
      RegisterCustomGirlRandomSoundC2SPacket::sounds,
      RegisterCustomGirlRandomSoundC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
