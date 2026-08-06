package com.cuddly.heartbound.networking.C2S;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record BonePosSyncC2SPacket(int entityId, Vec3 position) implements CustomPacketPayload {
   public static final Type<BonePosSyncC2SPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "sync_passenger_bone_pos"));
   public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3D_CODEC = StreamCodec.ofMember((vec, buf) -> {
      buf.writeDouble(vec.x);
      buf.writeDouble(vec.y);
      buf.writeDouble(vec.z);
   }, buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
   public static final StreamCodec<RegistryFriendlyByteBuf, BonePosSyncC2SPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, BonePosSyncC2SPacket::entityId, VEC3D_CODEC, BonePosSyncC2SPacket::position, BonePosSyncC2SPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
