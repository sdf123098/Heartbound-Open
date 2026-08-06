package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RunAnimEventsS2CPacket(int entityId, String event) implements CustomPacketPayload {
   public static final Type<RunAnimEventsS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "run_anim_events"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RunAnimEventsS2CPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, RunAnimEventsS2CPacket::entityId, ByteBufCodecs.STRING_UTF8, RunAnimEventsS2CPacket::event, RunAnimEventsS2CPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
