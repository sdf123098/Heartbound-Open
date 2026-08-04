package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformRevertC2SPacket() implements CustomPayload {
   public static final Id<TransformRevertC2SPacket> ID = new Id<>(Identifier.of("heartbound", "transform_revert"));
   public static final PacketCodec<RegistryByteBuf, TransformRevertC2SPacket> CODEC = PacketCodec.unit(new TransformRevertC2SPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
