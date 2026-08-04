package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record CumKeybindC2SPacket(Boolean pressed) implements CustomPayload {
   public static final Id<CumKeybindC2SPacket> ID = new Id<>(Identifier.of("heartbound", "cum_keybind_pressed"));
   public static final PacketCodec<RegistryByteBuf, CumKeybindC2SPacket> CODEC = new PacketCodec<RegistryByteBuf, CumKeybindC2SPacket>() {
      public CumKeybindC2SPacket decode(RegistryByteBuf buf) {
         return new CumKeybindC2SPacket(buf.readBoolean());
      }

      public void encode(RegistryByteBuf buf, CumKeybindC2SPacket value) {
         buf.writeBoolean(value.pressed());
      }
   };

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
