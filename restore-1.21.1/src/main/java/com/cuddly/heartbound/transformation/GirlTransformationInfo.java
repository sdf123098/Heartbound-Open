package com.cuddly.heartbound.transformation;

import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import java.util.LinkedHashMap;
import java.util.Map;

public record GirlTransformationInfo(String girlId, String displayNameKey, float width, float height, float eyeHeight, boolean isCustom) {
   public static final Map<String, GirlTransformationInfo> REGISTRY = new LinkedHashMap<>();

   public static void init() {
      register("jenny", "entity.heartbound.jenny", 0.5F, 1.95F, false);
      register("ellie", "entity.heartbound.ellie", 0.5F, 1.95F, false);
      register("bia", "entity.heartbound.bia", 0.5F, 1.65F, false);
      register("slime", "entity.heartbound.slime", 0.5F, 1.95F, false);
      register("kobold", "entity.heartbound.kobold", 0.5F, 1.75F, false);
      register("coppie", "entity.heartbound.coppie", 0.5F, 1.35F, false);
      register("aly", "entity.heartbound.aly", 0.5F, 1.65F, false);

      for (CustomGirlProfile profile : CustomGirlLoader.LOADED_PROFILES.values()) {
         register(profile.id(), profile.name(), 0.5F, profile.hitboxHeight(), true);
      }
   }

   private static void register(String girlId, String displayNameKey, float width, float height, boolean isCustom) {
      float eyeHeight = height * 0.9F;
      REGISTRY.put(girlId, new GirlTransformationInfo(girlId, displayNameKey, width, height, eyeHeight, isCustom));
   }
}
