package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record PlayerAnimLockC2SPacket(boolean locked) implements CustomPayload {
   public static final Id<PlayerAnimLockC2SPacket> ID = new Id<>(Identifier.of("heartbound", "player_anim_lock"));
   public static final PacketCodec<RegistryByteBuf, PlayerAnimLockC2SPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.BOOL, PlayerAnimLockC2SPacket::locked, PlayerAnimLockC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
