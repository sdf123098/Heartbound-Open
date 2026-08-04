package com.cuddly.heartbound.networking.C2S;

import com.cuddly.heartbound.util.variables.Scene;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformStartSceneC2SPacket(Scene scene) implements CustomPayload {
   public static final Id<TransformStartSceneC2SPacket> ID = new Id<>(Identifier.of("heartbound", "transform_start_scene"));
   public static final PacketCodec<RegistryByteBuf, TransformStartSceneC2SPacket> CODEC = PacketCodec.tuple(
      Scene.PACKET_CODEC, TransformStartSceneC2SPacket::scene, TransformStartSceneC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
