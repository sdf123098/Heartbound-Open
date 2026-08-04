package com.cuddly.heartbound.networking.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record RefreshModelsS2CPacket() implements CustomPayload {
   public static final Id<RefreshModelsS2CPacket> ID = new Id<>(Identifier.of("heartbound", "refresh_models"));
   public static final PacketCodec<RegistryByteBuf, RefreshModelsS2CPacket> CODEC = PacketCodec.unit(new RefreshModelsS2CPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
