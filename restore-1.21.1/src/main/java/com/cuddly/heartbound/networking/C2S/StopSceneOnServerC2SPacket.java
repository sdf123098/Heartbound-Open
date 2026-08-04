package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record StopSceneOnServerC2SPacket(int entityId) implements CustomPayload {
   public static final Id<StopSceneOnServerC2SPacket> ID = new Id<>(Identifier.of("heartbound", "stop_scene"));
   public static final PacketCodec<RegistryByteBuf, StopSceneOnServerC2SPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT, StopSceneOnServerC2SPacket::entityId, StopSceneOnServerC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
