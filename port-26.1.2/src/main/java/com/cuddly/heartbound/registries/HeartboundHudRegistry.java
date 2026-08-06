package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.gui.screen.hud.SceneProgressOverlay;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.variables.ScenePhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class HeartboundHudRegistry {
   private static final Identifier OVERLAY_ID = Identifier.fromNamespaceAndPath("heartbound", "scene_progress_overlay");
   private static int hbHudLogCounter = 0;

   private HeartboundHudRegistry() {
   }

   @Environment(EnvType.CLIENT)
   public static void register() {
      HudElementRegistry.addLast(OVERLAY_ID, HeartboundHudRegistry::render);
   }

   private static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
      Minecraft client = Minecraft.getInstance();
      Player localPlayer = client.player;
      if (localPlayer != null) {
         if (localPlayer.getVehicle() instanceof GirlSceneEntity scene) {
            if (scene.getAnimationKeyFrameEvent().contains("sexui")
               || scene.getCurrentScenePhase() == ScenePhase.HAVING_SEX
               || scene.getCurrentScenePhase() == ScenePhase.CUM) {
               SceneProgressOverlay.setActive(true);
            }

            SceneProgressOverlay.render(context, scene.getSceneProgress(), scene.getCumThreshold());
            if (++hbHudLogCounter % 20 == 0) {
               Heartbound.LOGGER.info(
                  "[HB-DBG] hud girlScene id={} phase={} hasSexui={} active={} prog={} thr={}",
                  scene.getId(),
                  scene.getCurrentScenePhase(),
                  scene.getAnimationKeyFrameEvent().contains("sexui"),
                  SceneProgressOverlay.isActive(),
                  scene.getSceneProgress(),
                  scene.getCumThreshold()
               );
            }
         } else {
            TransformablePlayer localTp = (TransformablePlayer)localPlayer;
            if (localTp.heartbound$isTransformSceneActive()) {
               ScenePhase phase = localTp.heartbound$getTransformScenePhase();
               if (phase == ScenePhase.HAVING_SEX || phase == ScenePhase.CUM) {
                  SceneProgressOverlay.setActive(true);
               }

               SceneProgressOverlay.render(context, localTp.heartbound$getTransformSceneProgress(), localTp.heartbound$getTransformCumThreshold());
               if (++hbHudLogCounter % 20 == 0) {
                  Heartbound.LOGGER.info(
                     "[HB-DBG] hud transform phase={} active={} prog={} thr={}",
                     phase,
                     SceneProgressOverlay.isActive(),
                     localTp.heartbound$getTransformSceneProgress(),
                     localTp.heartbound$getTransformCumThreshold()
                  );
               }
            } else {
               if (localPlayer.getVehicle() instanceof Player ridden) {
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
   }
}
