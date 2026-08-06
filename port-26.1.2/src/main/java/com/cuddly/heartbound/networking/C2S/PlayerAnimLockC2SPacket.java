package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayerAnimLockC2SPacket(boolean locked) implements CustomPacketPayload {
   public static final Type<PlayerAnimLockC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "player_anim_lock"));
   public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAnimLockC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL, PlayerAnimLockC2SPacket::locked, PlayerAnimLockC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
