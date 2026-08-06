package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenCustomizeScreenS2CPacket(int entityId, int previewEntityId) implements CustomPacketPayload {
   public static final Type<OpenCustomizeScreenS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "customize_screen"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenCustomizeScreenS2CPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      OpenCustomizeScreenS2CPacket::entityId,
      ByteBufCodecs.VAR_INT,
      OpenCustomizeScreenS2CPacket::previewEntityId,
      OpenCustomizeScreenS2CPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
