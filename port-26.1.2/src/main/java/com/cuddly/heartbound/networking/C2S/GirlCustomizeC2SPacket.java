package com.cuddly.heartbound.networking.C2S;

import com.cuddly.heartbound.registries.HeartboundTrackedDataRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record GirlCustomizeC2SPacket(int entityId, int breastSize, Vec3 breastOffset) implements CustomPacketPayload {
   public static final Type<GirlCustomizeC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "customize"));
   public static final StreamCodec<RegistryFriendlyByteBuf, GirlCustomizeC2SPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, GirlCustomizeC2SPacket>() {
      public GirlCustomizeC2SPacket decode(RegistryFriendlyByteBuf buf) {
         int entityId = buf.readVarInt();
         int breastSize = buf.readVarInt();
         Vec3 breastOffset = HeartboundTrackedDataRegistry.VEC3D_PACKET_CODEC.decode(buf);
         return new GirlCustomizeC2SPacket(entityId, breastSize, breastOffset);
      }

      public void encode(RegistryFriendlyByteBuf buf, GirlCustomizeC2SPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeVarInt(value.breastSize());
         HeartboundTrackedDataRegistry.VEC3D_PACKET_CODEC.encode(buf, value.breastOffset());
      }
   };

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
