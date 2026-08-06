package com.cuddly.heartbound.networking.C2S;

import com.cuddly.heartbound.util.variables.Scene;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StartSceneC2SPacket(int entityId, Scene scene) implements CustomPacketPayload {
   public static final Type<StartSceneC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "start_scene_from_client"));
   public static final StreamCodec<RegistryFriendlyByteBuf, StartSceneC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, StartSceneC2SPacket::entityId, Scene.PACKET_CODEC, StartSceneC2SPacket::scene, StartSceneC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
