package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record SoundEventSyncC2SPacket(int entityId, String soundEvent) implements CustomPayload {
   public static final Id<SoundEventSyncC2SPacket> ID = new Id<>(Identifier.of("heartbound", "sound_event_sync"));
   public static final PacketCodec<RegistryByteBuf, SoundEventSyncC2SPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT, SoundEventSyncC2SPacket::entityId, PacketCodecs.STRING, SoundEventSyncC2SPacket::soundEvent, SoundEventSyncC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
