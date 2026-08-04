package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformSceneKeybindC2SPacket() implements CustomPayload {
   public static final Id<TransformSceneKeybindC2SPacket> ID = new Id<>(Identifier.of("heartbound", "transform_scene_keybind"));
   public static final PacketCodec<ByteBuf, TransformSceneKeybindC2SPacket> CODEC = PacketCodec.unit(new TransformSceneKeybindC2SPacket());

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
