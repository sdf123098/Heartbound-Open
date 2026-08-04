package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.util.variables.Scene;
import com.cuddly.heartbound.util.variables.ScenePhase;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;

public class HeartboundTrackedDataRegistry {
   public static final PacketCodec<RegistryByteBuf, Vec3d> VEC3D_PACKET_CODEC = new PacketCodec<RegistryByteBuf, Vec3d>() {
      public Vec3d decode(RegistryByteBuf buf) {
         return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
      }

      public void encode(RegistryByteBuf buf, Vec3d value) {
         buf.writeDouble(value.x);
         buf.writeDouble(value.y);
         buf.writeDouble(value.z);
      }
   };
   public static final TrackedDataHandler<Scene> SCENE = TrackedDataHandler.create(Scene.PACKET_CODEC);
   public static final TrackedDataHandler<Optional<UUID>> OPTIONAL_UUID = TrackedDataHandler.create(Uuids.PACKET_CODEC.collect(PacketCodecs::optional));
   public static final TrackedDataHandler<UUID> UUID = TrackedDataHandler.create(Uuids.PACKET_CODEC);
   public static final TrackedDataHandler<ScenePhase> SCENE_PHASE = TrackedDataHandler.create(ScenePhase.PACKET_CODEC);
   public static final TrackedDataHandler<Vec3d> VEC3D = TrackedDataHandler.create(VEC3D_PACKET_CODEC);

   public static void registerTrackedData() {
      Heartbound.LOGGER.info("Registering custom TrackedDataHandlers for Heartbound");
      TrackedDataHandlerRegistry.register(SCENE);
      TrackedDataHandlerRegistry.register(OPTIONAL_UUID);
      TrackedDataHandlerRegistry.register(UUID);
      TrackedDataHandlerRegistry.register(SCENE_PHASE);
      TrackedDataHandlerRegistry.register(VEC3D);
   }
}
