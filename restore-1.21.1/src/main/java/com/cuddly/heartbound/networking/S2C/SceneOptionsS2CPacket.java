package com.cuddly.heartbound.networking.S2C;

import com.cuddly.heartbound.util.variables.Scene;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record SceneOptionsS2CPacket(int entityId, int currentRelationshipLevel, ItemStack attractedTo, List<Scene> options) implements CustomPayload {
   public static final Id<SceneOptionsS2CPacket> ID = new Id<>(Identifier.of("heartbound", "scene_options"));
   public static final PacketCodec<RegistryByteBuf, SceneOptionsS2CPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT,
      SceneOptionsS2CPacket::entityId,
      PacketCodecs.VAR_INT,
      SceneOptionsS2CPacket::currentRelationshipLevel,
      ItemStack.PACKET_CODEC,
      SceneOptionsS2CPacket::attractedTo,
      PacketCodecs.collection(ArrayList::new, Scene.PACKET_CODEC),
      SceneOptionsS2CPacket::options,
      SceneOptionsS2CPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
