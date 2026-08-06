package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record KoboldCustomizeC2SPacket(
   int entityId, int bodySize, int breastSize, int primaryColor, int secondaryColor, int irisColor, int topHornType, int bottomHornType
) implements CustomPacketPayload {
   public static final Type<KoboldCustomizeC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "kobold_customize"));
   public static final StreamCodec<RegistryFriendlyByteBuf, KoboldCustomizeC2SPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, KoboldCustomizeC2SPacket>() {
      public KoboldCustomizeC2SPacket decode(RegistryFriendlyByteBuf buf) {
         return new KoboldCustomizeC2SPacket(
            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()
         );
      }

      public void encode(RegistryFriendlyByteBuf buf, KoboldCustomizeC2SPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeVarInt(value.bodySize());
         buf.writeVarInt(value.breastSize());
         buf.writeVarInt(value.primaryColor());
         buf.writeVarInt(value.secondaryColor());
         buf.writeVarInt(value.irisColor());
         buf.writeVarInt(value.topHornType());
         buf.writeVarInt(value.bottomHornType());
      }
   };

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
