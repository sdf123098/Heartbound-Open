package com.cuddly.heartbound.component;

import com.cuddly.heartbound.Heartbound;
import com.mojang.serialization.Codec;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public class HeartboundDataComponentTypes {
   public static final DataComponentType<CompoundTag> STORED_ENTITY = register(
      "stored_entity", builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
   );
   public static final DataComponentType<String> GIRL_PROFILE_ID = register(
      "girl_profile_id", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
   );

   private static <T> DataComponentType<T> register(String name, UnaryOperator<Builder<T>> builderOperator) {
      return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("heartbound", name), builderOperator.apply(DataComponentType.builder()).build());
   }

   public static void registerDataComponentsTypes() {
      Heartbound.LOGGER.info("Registering DataComponentTypes for Heartbound");
   }
}
