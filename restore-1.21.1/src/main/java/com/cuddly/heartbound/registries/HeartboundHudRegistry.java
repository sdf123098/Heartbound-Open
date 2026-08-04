package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.client.gui.screen.hud.SceneProgressOverlay;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.variables.ScenePhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class HeartboundHudRegistry {
   private HeartboundHudRegistry() {
   }

   @Environment(EnvType.CLIENT)
   public static void register() {
      HudRenderCallback.EVENT.register((context, tickCounter) -> {
         MinecraftClient client = MinecraftClient.getInstance();
         PlayerEntity localPlayer = client.player;
         if (localPlayer != null) {
            if (localPlayer.getVehicle() instanceof GirlSceneEntity scene) {
               if (scene.getAnimationKeyFrameEvent().contains("sexui")) {
                  SceneProgressOverlay.setActive(true);
               }

               SceneProgressOverlay.render(context, scene.getSceneProgress(), scene.getCumThreshold());
            } else {
               TransformablePlayer localTp = (TransformablePlayer)localPlayer;
               if (localTp.heartbound$isTransformSceneActive()) {
                  ScenePhase phase = localTp.heartbound$getTransformScenePhase();
                  if (phase == ScenePhase.HAVING_SEX || phase == ScenePhase.CUM) {
                     SceneProgressOverlay.setActive(true);
                  }

                  SceneProgressOverlay.render(context, localTp.heartbound$getTransformSceneProgress(), localTp.heartbound$getTransformCumThreshold());
               } else {
                  if (localPlayer.getVehicle() instanceof PlayerEntity ridden) {
                     TransformablePlayer riddenTp = (TransformablePlayer)ridden;
                     if (riddenTp.heartbound$isTransformSceneActive()) {
                        ScenePhase phase = riddenTp.heartbound$getTransformScenePhase();
                        if (phase == ScenePhase.HAVING_SEX || phase == ScenePhase.CUM) {
                           SceneProgressOverlay.setActive(true);
                        }

                        SceneProgressOverlay.render(context, riddenTp.heartbound$getTransformSceneProgress(), riddenTp.heartbound$getTransformCumThreshold());
                        return;
                     }
                  }

                  SceneProgressOverlay.setActive(false);
               }
            }
         }
      });
   }
}
