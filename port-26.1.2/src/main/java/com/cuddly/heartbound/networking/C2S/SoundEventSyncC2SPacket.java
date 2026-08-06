package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SoundEventSyncC2SPacket(int entityId, String soundEvent) implements CustomPacketPayload {
   public static final Type<SoundEventSyncC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "sound_event_sync"));
   public static final StreamCodec<RegistryFriendlyByteBuf, SoundEventSyncC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, SoundEventSyncC2SPacket::entityId, ByteBufCodecs.STRING_UTF8, SoundEventSyncC2SPacket::soundEvent, SoundEventSyncC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
