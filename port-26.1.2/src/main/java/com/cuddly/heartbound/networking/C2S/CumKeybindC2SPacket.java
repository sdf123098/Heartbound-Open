package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CumKeybindC2SPacket(Boolean pressed) implements CustomPacketPayload {
   public static final Type<CumKeybindC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "cum_keybind_pressed"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CumKeybindC2SPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, CumKeybindC2SPacket>() {
      public CumKeybindC2SPacket decode(RegistryFriendlyByteBuf buf) {
         return new CumKeybindC2SPacket(buf.readBoolean());
      }

      public void encode(RegistryFriendlyByteBuf buf, CumKeybindC2SPacket value) {
         buf.writeBoolean(value.pressed());
      }
   };

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
