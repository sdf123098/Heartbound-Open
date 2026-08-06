package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.util.variables.Scene;
import com.cuddly.heartbound.util.variables.ScenePhase;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class HeartboundTrackedDataRegistry {
   public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3D_PACKET_CODEC = new StreamCodec<RegistryFriendlyByteBuf, Vec3>() {
      public Vec3 decode(RegistryFriendlyByteBuf buf) {
         return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
      }

      public void encode(RegistryFriendlyByteBuf buf, Vec3 value) {
         buf.writeDouble(value.x);
         buf.writeDouble(value.y);
         buf.writeDouble(value.z);
      }
   };
   public static final EntityDataSerializer<Scene> SCENE = EntityDataSerializer.forValueType(Scene.PACKET_CODEC);
   public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional));
   public static final EntityDataSerializer<UUID> UUID = EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC);
   public static final EntityDataSerializer<ScenePhase> SCENE_PHASE = EntityDataSerializer.forValueType(ScenePhase.PACKET_CODEC);
   public static final EntityDataSerializer<Vec3> VEC3D = EntityDataSerializer.forValueType(VEC3D_PACKET_CODEC);

   public static void registerTrackedData() {
      Heartbound.LOGGER.info("Registering custom TrackedDataHandlers for Heartbound");
      FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("heartbound", "scene"), SCENE);
      FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("heartbound", "optional_uuid"), OPTIONAL_UUID);
      FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("heartbound", "uuid"), UUID);
      FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("heartbound", "scene_phase"), SCENE_PHASE);
      FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("heartbound", "vec3d"), VEC3D);
   }
}
