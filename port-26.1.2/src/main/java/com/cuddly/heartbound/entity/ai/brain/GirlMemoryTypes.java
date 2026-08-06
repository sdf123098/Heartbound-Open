package com.cuddly.heartbound.entity.ai.brain;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.util.variables.Scene;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GirlMemoryTypes {
   public static final MemoryModuleType<Boolean> STRIP_REQUEST = register("strip_request");
   public static final MemoryModuleType<Scene> SCENE_OPTIONS = register("scene_options");

   private static <T> MemoryModuleType<T> register(String id) {
      return Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, Identifier.fromNamespaceAndPath("heartbound", id), new MemoryModuleType<>(Optional.empty()));
   }

   private static <T> MemoryModuleType<T> register(String id, Codec<T> codec) {
      return Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, Identifier.fromNamespaceAndPath("heartbound", id), new MemoryModuleType<>(Optional.of(codec)));
   }

   public static void registerMemoryTypes() {
      Heartbound.LOGGER.info("Registering MemoryTypes of Heartbound");
   }
}
