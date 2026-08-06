package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ThrustKeybindC2SPacket(Boolean held) implements CustomPacketPayload {
   public static final Type<ThrustKeybindC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "thrust_keybind_held"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ThrustKeybindC2SPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, ThrustKeybindC2SPacket>() {
      public ThrustKeybindC2SPacket decode(RegistryFriendlyByteBuf buf) {
         return new ThrustKeybindC2SPacket(buf.readBoolean());
      }

      public void encode(RegistryFriendlyByteBuf buf, ThrustKeybindC2SPacket value) {
         buf.writeBoolean(value.held());
      }
   };

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
