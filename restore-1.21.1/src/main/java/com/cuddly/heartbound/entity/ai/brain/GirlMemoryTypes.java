package com.cuddly.heartbound.entity.ai.brain;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.util.variables.Scene;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GirlMemoryTypes {
   public static final MemoryModuleType<Boolean> STRIP_REQUEST = register("strip_request");
   public static final MemoryModuleType<Scene> SCENE_OPTIONS = register("scene_options");

   private static <T> MemoryModuleType<T> register(String id) {
      return Registry.register(Registries.MEMORY_MODULE_TYPE, Identifier.of("heartbound", id), new MemoryModuleType<>(Optional.empty()));
   }

   private static <T> MemoryModuleType<T> register(String id, Codec<T> codec) {
      return Registry.register(Registries.MEMORY_MODULE_TYPE, Identifier.of("heartbound", id), new MemoryModuleType<>(Optional.of(codec)));
   }

   public static void registerMemoryTypes() {
      Heartbound.LOGGER.info("Registering MemoryTypes of Heartbound");
   }
}
