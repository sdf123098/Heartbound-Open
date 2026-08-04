package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record PlayAttackAnimationS2CPacket(int entityId) implements CustomPayload {
   public static final Id<PlayAttackAnimationS2CPacket> ID = new Id<>(Identifier.of("heartbound", "play_attack_animation"));
   public static final PacketCodec<RegistryByteBuf, PlayAttackAnimationS2CPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT, PlayAttackAnimationS2CPacket::entityId, PlayAttackAnimationS2CPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
