package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StopSceneOnServerC2SPacket(int entityId) implements CustomPacketPayload {
   public static final Type<StopSceneOnServerC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "stop_scene"));
   public static final StreamCodec<RegistryFriendlyByteBuf, StopSceneOnServerC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, StopSceneOnServerC2SPacket::entityId, StopSceneOnServerC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
