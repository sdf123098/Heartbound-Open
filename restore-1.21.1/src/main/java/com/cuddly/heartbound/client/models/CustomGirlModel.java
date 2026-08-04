package com.cuddly.heartbound.client.models;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.ClientUtils;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class CustomGirlModel extends AbstractGirlModel<CustomGirlEntity> {
   private final Map<String, Boolean> fallbackUsed = new HashMap<>();

   public Identifier getModelResource(CustomGirlEntity animatable) {
      boolean stripped = animatable.isStripped();
      String girlID = animatable.getGirlID();
      String folder = stripped ? "nude/" : "dressed/";
      String filePath = "geo/" + folder + girlID + ".geo.json";
      boolean inGui = MinecraftClient.getInstance().currentScreen != null;
      boolean exists = ClientUtils.assetExistsClient(Identifier.of("heartbound", filePath));
      if (!exists) {
         this.fallbackUsed.put(girlID, true);
         if (!inGui) {
            Heartbound.LOGGER.error("Model files for " + girlID + " doesn't exist");
         }

         return Identifier.of("heartbound", "geo/" + folder + "default.geo.json");
      } else if (inGui && this.fallbackUsed.getOrDefault(girlID, false)) {
         return Identifier.of("heartbound", "geo/" + folder + "default.geo.json");
      } else {
         this.fallbackUsed.put(girlID, false);
         return super.getModelResource(animatable);
      }
   }

   public Identifier getAnimationResource(CustomGirlEntity animatable) {
      String folder = "animations/";
      String filePath = folder + animatable.getGirlID() + ".animation.json";
      return ClientUtils.assetExistsClient(Identifier.of("heartbound", filePath)) ? super.getAnimationResource(animatable) : null;
   }

   public Identifier getTextureResource(CustomGirlEntity animatable) {
      String girlID = animatable.getGirlID();
      String folder = "textures/entities/";
      String filePath = folder + girlID + ".png";
      return ClientUtils.assetExistsClient(Identifier.of("heartbound", filePath))
         ? super.getTextureResource(animatable)
         : Identifier.of("heartbound", folder + "default.png");
   }
}
