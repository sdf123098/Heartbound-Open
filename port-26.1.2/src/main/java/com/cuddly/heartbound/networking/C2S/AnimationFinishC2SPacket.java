package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AnimationFinishC2SPacket(int entityId) implements CustomPacketPayload {
   public static final Type<AnimationFinishC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "finished_anim"));
   public static final StreamCodec<RegistryFriendlyByteBuf, AnimationFinishC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, AnimationFinishC2SPacket::entityId, AnimationFinishC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
