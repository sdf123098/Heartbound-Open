package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record ThrustKeybindC2SPacket(Boolean held) implements CustomPayload {
   public static final Id<ThrustKeybindC2SPacket> ID = new Id<>(Identifier.of("heartbound", "thrust_keybind_held"));
   public static final PacketCodec<RegistryByteBuf, ThrustKeybindC2SPacket> CODEC = new PacketCodec<RegistryByteBuf, ThrustKeybindC2SPacket>() {
      public ThrustKeybindC2SPacket decode(RegistryByteBuf buf) {
         return new ThrustKeybindC2SPacket(buf.readBoolean());
      }

      public void encode(RegistryByteBuf buf, ThrustKeybindC2SPacket value) {
         buf.writeBoolean(value.held());
      }
   };

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
