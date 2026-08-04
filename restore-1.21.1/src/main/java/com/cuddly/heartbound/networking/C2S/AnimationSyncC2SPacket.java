package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record AnimationSyncC2SPacket(int entityId, String animationState, boolean loopState, boolean holdState) implements CustomPayload {
   public static final Id<AnimationSyncC2SPacket> ID = new Id<>(Identifier.of("heartbound", "sync_override_animation"));
   public static final PacketCodec<RegistryByteBuf, AnimationSyncC2SPacket> CODEC = new PacketCodec<RegistryByteBuf, AnimationSyncC2SPacket>() {
      public AnimationSyncC2SPacket decode(RegistryByteBuf buf) {
         return new AnimationSyncC2SPacket(buf.readVarInt(), buf.readString(), buf.readBoolean(), buf.readBoolean());
      }

      public void encode(RegistryByteBuf buf, AnimationSyncC2SPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeString(value.animationState());
         buf.writeBoolean(value.loopState());
         buf.writeBoolean(value.holdState());
      }
   };

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
