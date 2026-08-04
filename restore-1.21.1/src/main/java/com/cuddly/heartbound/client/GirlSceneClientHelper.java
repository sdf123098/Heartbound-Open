package com.cuddly.heartbound.client;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class GirlSceneClientHelper {
   private GirlSceneClientHelper() {
   }

   public static void applySkinToBone(GirlSceneEntity girl, PlayerEntity player) {
      if (girl.getWorld().isClient()) {
         girl.overrideBoneTexture("steve", Identifier.ofVanilla("textures/entity/player/wide/steve.png"));
         if (player instanceof AbstractClientPlayerEntity clientPlayer) {
            SkinTextures skin = clientPlayer.getSkinTextures();
            Identifier texture = skin.texture();
            girl.setIsPlayerModelSlim(skin.model() == Model.SLIM);
            if (texture != null) {
               girl.overrideBoneTexture("steve", texture);
            }

            girl.overrideBoneTextureLayer2("steve", Identifier.of("heartbound", "textures/player/penis.png"));
         }
      }
   }
}
