package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record SetGUIOpenStateC2SPacket(int entityId, boolean data) implements CustomPayload {
   public static final Id<SetGUIOpenStateC2SPacket> ID = new Id<>(Identifier.of("heartbound", "in_inventory"));
   public static final PacketCodec<RegistryByteBuf, SetGUIOpenStateC2SPacket> CODEC = new PacketCodec<RegistryByteBuf, SetGUIOpenStateC2SPacket>() {
      public SetGUIOpenStateC2SPacket decode(RegistryByteBuf buf) {
         return new SetGUIOpenStateC2SPacket(buf.readVarInt(), buf.readBoolean());
      }

      public void encode(RegistryByteBuf buf, SetGUIOpenStateC2SPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeBoolean(value.data());
      }
   };

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
