package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record OpenKoboldCustomizeScreenS2CPacket(int entityId, int previewEntityId) implements CustomPayload {
   public static final Id<OpenKoboldCustomizeScreenS2CPacket> ID = new Id<>(Identifier.of("heartbound", "customize_kobold_screen"));
   public static final PacketCodec<RegistryByteBuf, OpenKoboldCustomizeScreenS2CPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT,
      OpenKoboldCustomizeScreenS2CPacket::entityId,
      PacketCodecs.VAR_INT,
      OpenKoboldCustomizeScreenS2CPacket::previewEntityId,
      OpenKoboldCustomizeScreenS2CPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
