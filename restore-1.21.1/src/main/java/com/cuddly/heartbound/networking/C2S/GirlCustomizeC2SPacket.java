package com.cuddly.heartbound.networking.C2S;

import com.cuddly.heartbound.registries.HeartboundTrackedDataRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record GirlCustomizeC2SPacket(int entityId, int breastSize, Vec3d breastOffset) implements CustomPayload {
   public static final Id<GirlCustomizeC2SPacket> ID = new Id<>(Identifier.of("heartbound", "customize"));
   public static final PacketCodec<RegistryByteBuf, GirlCustomizeC2SPacket> CODEC = new PacketCodec<RegistryByteBuf, GirlCustomizeC2SPacket>() {
      public GirlCustomizeC2SPacket decode(RegistryByteBuf buf) {
         int entityId = buf.readVarInt();
         int breastSize = buf.readVarInt();
         Vec3d breastOffset = HeartboundTrackedDataRegistry.VEC3D_PACKET_CODEC.decode(buf);
         return new GirlCustomizeC2SPacket(entityId, breastSize, breastOffset);
      }

      public void encode(RegistryByteBuf buf, GirlCustomizeC2SPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeVarInt(value.breastSize());
         HeartboundTrackedDataRegistry.VEC3D_PACKET_CODEC.encode(buf, value.breastOffset());
      }
   };

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
