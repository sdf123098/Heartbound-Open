package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record OpenCustomizeScreenS2CPacket(int entityId, int previewEntityId) implements CustomPayload {
   public static final Id<OpenCustomizeScreenS2CPacket> ID = new Id<>(Identifier.of("heartbound", "customize_screen"));
   public static final PacketCodec<RegistryByteBuf, OpenCustomizeScreenS2CPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT,
      OpenCustomizeScreenS2CPacket::entityId,
      PacketCodecs.VAR_INT,
      OpenCustomizeScreenS2CPacket::previewEntityId,
      OpenCustomizeScreenS2CPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
