package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record KoboldCustomizeC2SPacket(
   int entityId, int bodySize, int breastSize, int primaryColor, int secondaryColor, int irisColor, int topHornType, int bottomHornType
) implements CustomPayload {
   public static final Id<KoboldCustomizeC2SPacket> ID = new Id<>(Identifier.of("heartbound", "kobold_customize"));
   public static final PacketCodec<RegistryByteBuf, KoboldCustomizeC2SPacket> CODEC = new PacketCodec<RegistryByteBuf, KoboldCustomizeC2SPacket>() {
      public KoboldCustomizeC2SPacket decode(RegistryByteBuf buf) {
         return new KoboldCustomizeC2SPacket(
            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()
         );
      }

      public void encode(RegistryByteBuf buf, KoboldCustomizeC2SPacket value) {
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
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
