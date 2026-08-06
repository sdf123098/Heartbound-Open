package com.cuddly.heartbound.networking.C2S;

import com.cuddly.heartbound.util.variables.ScenePhase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ScenePhaseSyncC2SPacket(int entityId, ScenePhase phase) implements CustomPacketPayload {
   public static final Type<ScenePhaseSyncC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "sync_scene_phase"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ScenePhaseSyncC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, ScenePhaseSyncC2SPacket::entityId, ScenePhase.PACKET_CODEC, ScenePhaseSyncC2SPacket::phase, ScenePhaseSyncC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
