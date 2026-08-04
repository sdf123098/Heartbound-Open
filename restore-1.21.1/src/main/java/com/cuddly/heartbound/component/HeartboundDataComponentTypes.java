package com.cuddly.heartbound.component;

import com.cuddly.heartbound.Heartbound;
import com.mojang.serialization.Codec;
import java.util.function.UnaryOperator;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentType.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class HeartboundDataComponentTypes {
   public static final ComponentType<NbtCompound> STORED_ENTITY = register(
      "stored_entity", builder -> builder.codec(NbtCompound.CODEC).packetCodec(PacketCodecs.NBT_COMPOUND)
   );
   public static final ComponentType<String> GIRL_PROFILE_ID = register(
      "girl_profile_id", builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING)
   );

   private static <T> ComponentType<T> register(String name, UnaryOperator<Builder<T>> builderOperator) {
      return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of("heartbound", name), builderOperator.apply(ComponentType.builder()).build());
   }

   public static void registerDataComponentsTypes() {
      Heartbound.LOGGER.info("Registering DataComponentTypes for Heartbound");
   }
}
