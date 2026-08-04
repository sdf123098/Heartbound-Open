package com.cuddly.heartbound.transformation;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;

public class TransformSceneRegistry {
   private static final Map<String, List<Scene>> CACHE = new HashMap<>();

   public static List<Scene> getScenes(String girlId, ServerWorld world) {
      return CACHE.computeIfAbsent(girlId, id -> computeScenes(id, world));
   }

   private static List<Scene> computeScenes(String girlId, ServerWorld world) {
      EntityType<?> type = getEntityType(girlId);
      if (type != null) {
         Entity entity = type.create(world);
         if (entity instanceof GirlSceneEntity girl) {
            List<Scene> scenes = girl.getScenes();
            entity.discard();
            return scenes;
         }
      }

      CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(girlId);
      return profile != null ? profile.scenes() : List.of();
   }

   private static EntityType<?> getEntityType(String girlId) {
      return switch (girlId) {
         case "jenny" -> GirlRegistry.JENNY;
         case "ellie" -> GirlRegistry.ELLIE;
         case "bia" -> GirlRegistry.BIA;
         case "slime" -> GirlRegistry.SLIME;
         case "kobold" -> GirlRegistry.KOBOLD;
         case "coppie" -> GirlRegistry.COPPIE;
         case "aly" -> GirlRegistry.ALY;
         default -> null;
      };
   }

   public static void clearCache() {
      CACHE.clear();
   }
}
