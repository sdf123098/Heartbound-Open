package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record AnimationFinishC2SPacket(int entityId) implements CustomPayload {
   public static final Id<AnimationFinishC2SPacket> ID = new Id<>(Identifier.of("heartbound", "finished_anim"));
   public static final PacketCodec<RegistryByteBuf, AnimationFinishC2SPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT, AnimationFinishC2SPacket::entityId, AnimationFinishC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
