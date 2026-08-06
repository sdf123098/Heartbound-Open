package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AnimationSyncC2SPacket(int entityId, String animationState, boolean loopState, boolean holdState) implements CustomPacketPayload {
   public static final Type<AnimationSyncC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "sync_override_animation"));
   public static final StreamCodec<RegistryFriendlyByteBuf, AnimationSyncC2SPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, AnimationSyncC2SPacket>() {
      public AnimationSyncC2SPacket decode(RegistryFriendlyByteBuf buf) {
         return new AnimationSyncC2SPacket(buf.readVarInt(), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
      }

      public void encode(RegistryFriendlyByteBuf buf, AnimationSyncC2SPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeUtf(value.animationState());
         buf.writeBoolean(value.loopState());
         buf.writeBoolean(value.holdState());
      }
   };

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
