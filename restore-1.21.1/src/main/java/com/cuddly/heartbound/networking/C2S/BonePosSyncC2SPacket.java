package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record BonePosSyncC2SPacket(int entityId, Vec3d position) implements CustomPayload {
   public static final Id<BonePosSyncC2SPacket> ID = new Id<>(Identifier.of("heartbound", "sync_passenger_bone_pos"));
   public static final PacketCodec<RegistryByteBuf, Vec3d> VEC3D_CODEC = PacketCodec.of((vec, buf) -> {
      buf.writeDouble(vec.x);
      buf.writeDouble(vec.y);
      buf.writeDouble(vec.z);
   }, buf -> new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
   public static final PacketCodec<RegistryByteBuf, BonePosSyncC2SPacket> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT, BonePosSyncC2SPacket::entityId, VEC3D_CODEC, BonePosSyncC2SPacket::position, BonePosSyncC2SPacket::new
   );

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
