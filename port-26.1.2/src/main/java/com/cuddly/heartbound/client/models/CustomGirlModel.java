package com.cuddly.heartbound.client.models;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.ClientUtils;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import com.geckolib.renderer.base.GeoRenderState;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class CustomGirlModel extends AbstractGirlModel<CustomGirlEntity> {
   private final Map<String, Boolean> fallbackUsed = new HashMap<>();

   @Override
   public Identifier getModelResource(GeoRenderState renderState) {
      CustomGirlEntity animatable = (CustomGirlEntity)this.getAnimatableFrom(renderState);
      if (animatable == null) {
         return Identifier.fromNamespaceAndPath("heartbound", "dressed/default");
      }

      boolean stripped = animatable.isStripped();
      String girlID = animatable.getGirlID();
      String folder = stripped ? "nude/" : "dressed/";
      String filePath = "geckolib/models/" + folder + girlID + ".geo.json";
      boolean inGui = Minecraft.getInstance().screen != null;
      boolean exists = ClientUtils.assetExistsClient(Identifier.fromNamespaceAndPath("heartbound", filePath));
      if (!exists) {
         this.fallbackUsed.put(girlID, true);
         if (!inGui) {
            Heartbound.LOGGER.error("Model files for " + girlID + " doesn't exist");
         }

         return Identifier.fromNamespaceAndPath("heartbound", folder + "default");
      } else if (inGui && this.fallbackUsed.getOrDefault(girlID, false)) {
         return Identifier.fromNamespaceAndPath("heartbound", folder + "default");
      } else {
         this.fallbackUsed.put(girlID, false);
         return super.getModelResource(renderState);
      }
   }

   public Identifier getAnimationResource(CustomGirlEntity animatable) {
      String filePath = "geckolib/animations/" + animatable.getGirlID() + ".animation.json";
      return ClientUtils.assetExistsClient(Identifier.fromNamespaceAndPath("heartbound", filePath)) ? super.getAnimationResource(animatable) : null;
   }

   @Override
   public Identifier getTextureResource(GeoRenderState renderState) {
      CustomGirlEntity animatable = (CustomGirlEntity)this.getAnimatableFrom(renderState);
      if (animatable == null) {
         return Identifier.fromNamespaceAndPath("heartbound", "textures/entities/default.png");
      }

      String girlID = animatable.getGirlID();
      String folder = "textures/entities/";
      String filePath = folder + girlID + ".png";
      return ClientUtils.assetExistsClient(Identifier.fromNamespaceAndPath("heartbound", filePath))
         ? super.getTextureResource(renderState)
         : Identifier.fromNamespaceAndPath("heartbound", folder + "default.png");
   }
}
