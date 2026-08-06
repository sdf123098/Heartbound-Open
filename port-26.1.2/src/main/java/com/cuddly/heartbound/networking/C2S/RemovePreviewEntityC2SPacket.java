package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RemovePreviewEntityC2SPacket(int entityId, int previewEntityId) implements CustomPacketPayload {
   public static final Type<RemovePreviewEntityC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "remove_preview_entity"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RemovePreviewEntityC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      RemovePreviewEntityC2SPacket::entityId,
      ByteBufCodecs.VAR_INT,
      RemovePreviewEntityC2SPacket::previewEntityId,
      RemovePreviewEntityC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
