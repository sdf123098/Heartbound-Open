package com.cuddly.heartbound.networking.S2C;

import com.cuddly.heartbound.util.variables.Scene;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransformSceneOptionsS2CPacket(List<Scene> scenes) implements CustomPacketPayload {
   public static final Type<TransformSceneOptionsS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "transform_scene_options"));
   public static final StreamCodec<RegistryFriendlyByteBuf, TransformSceneOptionsS2CPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.collection(ArrayList::new, Scene.PACKET_CODEC), TransformSceneOptionsS2CPacket::scenes, TransformSceneOptionsS2CPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
