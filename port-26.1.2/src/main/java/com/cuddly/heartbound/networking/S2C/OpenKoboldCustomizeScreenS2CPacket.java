package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenKoboldCustomizeScreenS2CPacket(int entityId, int previewEntityId) implements CustomPacketPayload {
   public static final Type<OpenKoboldCustomizeScreenS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "customize_kobold_screen"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenKoboldCustomizeScreenS2CPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      OpenKoboldCustomizeScreenS2CPacket::entityId,
      ByteBufCodecs.VAR_INT,
      OpenKoboldCustomizeScreenS2CPacket::previewEntityId,
      OpenKoboldCustomizeScreenS2CPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
