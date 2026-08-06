package com.cuddly.heartbound.networking.C2S;

import com.cuddly.heartbound.util.variables.Scene;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformStartSceneC2SPacket(Scene scene) implements CustomPacketPayload {
   public static final Type<TransformStartSceneC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_start_scene"));
   public static final StreamCodec<RegistryFriendlyByteBuf, TransformStartSceneC2SPacket> CODEC = StreamCodec.composite(
      Scene.PACKET_CODEC, TransformStartSceneC2SPacket::scene, TransformStartSceneC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
