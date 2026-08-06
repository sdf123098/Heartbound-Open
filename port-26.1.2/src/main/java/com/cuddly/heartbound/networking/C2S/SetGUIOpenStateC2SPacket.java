package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetGUIOpenStateC2SPacket(int entityId, boolean data) implements CustomPacketPayload {
   public static final Type<SetGUIOpenStateC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "in_inventory"));
   public static final StreamCodec<RegistryFriendlyByteBuf, SetGUIOpenStateC2SPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, SetGUIOpenStateC2SPacket>() {
      public SetGUIOpenStateC2SPacket decode(RegistryFriendlyByteBuf buf) {
         return new SetGUIOpenStateC2SPacket(buf.readVarInt(), buf.readBoolean());
      }

      public void encode(RegistryFriendlyByteBuf buf, SetGUIOpenStateC2SPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeBoolean(value.data());
      }
   };

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
