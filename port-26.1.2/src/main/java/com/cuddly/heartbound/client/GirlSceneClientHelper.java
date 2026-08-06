package com.cuddly.heartbound.client;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public final class GirlSceneClientHelper {
   private GirlSceneClientHelper() {
   }

   public static void applySkinToBone(GirlSceneEntity girl, Player player) {
      if (girl.level().isClientSide()) {
         girl.overrideBoneTexture("steve", Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png"));
         if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerSkin skin = clientPlayer.getSkin();
            Identifier texture = skin.body().texturePath();
            girl.setIsPlayerModelSlim(skin.model() == PlayerModelType.SLIM);
            if (texture != null) {
               girl.overrideBoneTexture("steve", texture);
            }

            girl.overrideBoneTextureLayer2("steve", Identifier.fromNamespaceAndPath("heartbound", "textures/player/penis.png"));
         }
      }
   }
}
