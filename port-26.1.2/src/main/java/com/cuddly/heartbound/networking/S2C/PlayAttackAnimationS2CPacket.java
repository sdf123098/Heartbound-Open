package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayAttackAnimationS2CPacket(int entityId) implements CustomPacketPayload {
   public static final Type<PlayAttackAnimationS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "play_attack_animation"));
   public static final StreamCodec<RegistryFriendlyByteBuf, PlayAttackAnimationS2CPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, PlayAttackAnimationS2CPacket::entityId, PlayAttackAnimationS2CPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
