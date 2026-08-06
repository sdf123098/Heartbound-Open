package com.cuddly.heartbound.networking.S2C;

import com.cuddly.heartbound.util.variables.Scene;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record SceneOptionsS2CPacket(int entityId, int currentRelationshipLevel, ItemStack attractedTo, List<Scene> options) implements CustomPacketPayload {
   public static final Type<SceneOptionsS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "scene_options"));
   public static final StreamCodec<RegistryFriendlyByteBuf, SceneOptionsS2CPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      SceneOptionsS2CPacket::entityId,
      ByteBufCodecs.VAR_INT,
      SceneOptionsS2CPacket::currentRelationshipLevel,
      ItemStack.STREAM_CODEC,
      SceneOptionsS2CPacket::attractedTo,
      ByteBufCodecs.collection(ArrayList::new, Scene.PACKET_CODEC),
      SceneOptionsS2CPacket::options,
      SceneOptionsS2CPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
