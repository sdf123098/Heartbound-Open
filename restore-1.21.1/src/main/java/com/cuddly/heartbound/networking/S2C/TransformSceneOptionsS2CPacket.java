package com.cuddly.heartbound.networking.S2C;

import com.cuddly.heartbound.util.variables.Scene;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record TransformSceneOptionsS2CPacket(List<Scene> scenes) implements CustomPayload {
   public static final Id<TransformSceneOptionsS2CPacket> ID = new Id<>(Identifier.of("heartbound", "transform_scene_options"));
   public static final PacketCodec<RegistryByteBuf, TransformSceneOptionsS2CPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.collection(ArrayList::new, Scene.PACKET_CODEC), TransformSceneOptionsS2CPacket::scenes, TransformSceneOptionsS2CPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
