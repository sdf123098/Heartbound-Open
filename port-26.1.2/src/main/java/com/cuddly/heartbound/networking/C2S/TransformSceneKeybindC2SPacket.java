package com.cuddly.heartbound.networking.C2S;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformSceneKeybindC2SPacket() implements CustomPacketPayload {
   public static final Type<TransformSceneKeybindC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_scene_keybind"));
   public static final StreamCodec<ByteBuf, TransformSceneKeybindC2SPacket> CODEC = StreamCodec.unit(new TransformSceneKeybindC2SPacket());

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
