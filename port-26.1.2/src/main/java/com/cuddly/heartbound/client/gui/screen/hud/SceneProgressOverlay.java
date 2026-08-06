package com.cuddly.heartbound.client.gui.screen.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class SceneProgressOverlay {
   private static final Identifier SCENE_PROGRESS_BAR_TEXTURE = Identifier.fromNamespaceAndPath("heartbound", "textures/gui/scene_progress_bar.png");
   private static final Identifier READY_TO_CUM_TEXTURE = Identifier.fromNamespaceAndPath("heartbound", "textures/gui/cum_button.png");
   private static boolean animatingCum = false;
   private static long cumStartTime = 0L;
   private static final long CUM_ANIM_DURATION = 500L;
   private static boolean active = false;

   public static void setActive(boolean on) {
      active = on;
   }

   public static boolean isActive() {
      return active;
   }

   public static void triggerCumAnimation() {
      animatingCum = true;
      cumStartTime = System.currentTimeMillis();
   }

   public static void render(GuiGraphicsExtractor context, float sceneProgress, float cumThreshold) {
      if (active) {
         float ratio = cumThreshold > 0.0F ? sceneProgress / cumThreshold : 0.0F;
         ratio = Math.min(ratio, 1.0F);
         int texWidth = 48;
         int texHeight = 175;
         float scale = 1.0F;
         int scaledWidth = (int)((float)texWidth * scale);
         int scaledHeight = (int)((float)texHeight * scale);
         int x = 36;
         int y = 10;
         context.blit(SCENE_PROGRESS_BAR_TEXTURE, x, y, scaledWidth, scaledHeight, 0.0F, 0.0F, 1.0F, 1.0F);
         float cumScale = 0.4F;
         int cumXPadding = 10;
         int cumU = 0;
         int cumV = 0;
         int cropWidth = (int)(256.0F * cumScale);
         int cropHeight = (int)(52.0F * cumScale);
         int cumWidth = (int)(256.0F * cumScale);
         int cumHeight = (int)(106.0F * cumScale);
         int cumYPadding = texHeight + 5;
         if (ratio == 1.0F) {
            cumV = (int)(55.0F * cumScale);
         }

         if (!animatingCum) {
            context.blit(
               READY_TO_CUM_TEXTURE,
               cumXPadding,
               y + cumYPadding,
               cropWidth,
               cropHeight,
               (float)cumU / (float)cumWidth,
               (float)cumV / (float)cumHeight,
               (float)cropWidth / (float)cumWidth,
               (float)cropHeight / (float)cumHeight
            );
         }

         int insetX = (int)(8.0F * scale);
         int insetY = (int)(8.0F * scale);
         int fillWidth = scaledWidth - insetX * 2;
         int fillHeightMax = scaledHeight - insetY * 2;
         int fillX = x + insetX;
         int baseFillBottom = y + scaledHeight - insetY;
         if (animatingCum) {
            long elapsed = System.currentTimeMillis() - cumStartTime;
            float t = Math.min((float)elapsed / 500.0F, 1.0F);
            int yOffset = (int)((float)scaledHeight * t * 2.0F);
            int fillY = baseFillBottom - fillHeightMax - yOffset;
            int color = -1;
            context.fill(fillX, fillY, fillX + fillWidth, baseFillBottom - yOffset, color);
            if (t >= 1.0F) {
               setActive(false);
               animatingCum = false;
            }
         } else {
            int filledHeight = (int)(ratio * (float)fillHeightMax);
            if (filledHeight > 0) {
               int fillY = baseFillBottom - filledHeight;
               int color = -269488145;
               context.fill(fillX, fillY, fillX + fillWidth, baseFillBottom, color);
            }
         }
      }
   }
}
