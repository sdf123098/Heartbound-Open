package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.networking.C2S.TransformStartSceneC2SPacket;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TransformSceneScreen extends Screen {
   private static final int COLS = 2;
   private static final int ROWS = 3;
   private static final int PER_PAGE = 6;
   private static final int BTN_W = 130;
   private static final int BTN_H = 24;
   private static final int BTN_GAP_X = 14;
   private static final int BTN_GAP_Y = 10;
   private static final int PADDING = 18;
   private static final int ARROW_W = 22;
   private static final int ARROW_H = 30;
   private static final int TEXT_MID = -9741750;
   private final List<Scene> scenes;
   private int page = 0;
   private int panelX;
   private int panelY;
   private int panelW;
   private int panelH;
   private int gridX;
   private int gridY;
   private int leftArrowX;
   private int leftArrowY;
   private int rightArrowX;
   private int rightArrowY;

   public TransformSceneScreen(List<Scene> scenes) {
      super(Component.translatable("gui.heartbound.sceneOptions"));
      this.scenes = scenes;
   }

   private int maxPage() {
      return Math.max(0, (this.scenes.size() - 1) / 6);
   }

   @Override
   protected void init() {
      super.init();
      this.calcLayout();
   }

   private void calcLayout() {
      int gridW = 274;
      int gridH = 92;
      int titleHeight = 28;
      this.panelW = gridW + 36 + 64;
      this.panelH = titleHeight + gridH + 36;
      this.panelX = (this.width - this.panelW) / 2;
      this.panelY = (this.height - this.panelH) / 2;
      this.gridX = this.panelX + 18 + 22 + 10;
      this.gridY = this.panelY + 18 + titleHeight;
      int contentMidY = this.gridY + (this.panelY + this.panelH - 18 - this.gridY) / 2;
      this.leftArrowX = this.panelX + 9;
      this.leftArrowY = contentMidY - 15;
      this.rightArrowX = this.panelX + this.panelW - 9 - 22;
      this.rightArrowY = this.leftArrowY;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      ScreenDecorations.drawBackground(context, this.width, this.height);
      ScreenDecorations.drawPanel(context, this.panelX, this.panelY, this.panelW, this.panelH);
      ScreenDecorations.drawTitleWithDecor(context, this.font, this.title, this.width / 2, this.panelY + 18);
      int startIdx = this.page * 6;

      for (int slot = 0; slot < 6; slot++) {
         int idx = startIdx + slot;
         int col = slot % 2;
         int row = slot / 2;
         int bx = this.gridX + col * 144;
         int by = this.gridY + row * 34;
         if (idx < this.scenes.size()) {
            Component label = Component.translatable(this.scenes.get(idx).displayName());
            ScreenDecorations.drawButton(context, this.font, bx, by, 130, 24, label, mouseX, mouseY, true);
         }
      }

      boolean canLeft = this.page > 0;
      boolean canRight = this.page < this.maxPage();
      ScreenDecorations.drawButton(context, this.font, this.leftArrowX, this.leftArrowY, 22, 30, Component.literal("◀"), mouseX, mouseY, canLeft);
      ScreenDecorations.drawButton(context, this.font, this.rightArrowX, this.rightArrowY, 22, 30, Component.literal("▶"), mouseX, mouseY, canRight);
      if (this.maxPage() > 0) {
         String pageText = this.page + 1 + "/" + (this.maxPage() + 1);
         int pw = this.font.width(pageText);
         context.text(this.font, pageText, (this.width - pw) / 2, this.panelY + this.panelH - 18 + 2, -9741750, false);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean onPress) {
      if (event.button() == 0) {
         int startIdx = this.page * 6;

         for (int slot = 0; slot < 6; slot++) {
            int idx = startIdx + slot;
            if (idx >= this.scenes.size()) {
               break;
            }

            int col = slot % 2;
            int row = slot / 2;
            int bx = this.gridX + col * 144;
            int by = this.gridY + row * 34;
            if (event.x() >= (double)bx && event.x() < (double)(bx + 130) && event.y() >= (double)by && event.y() < (double)(by + 24)) {
               ClientPlayNetworking.send(new TransformStartSceneC2SPacket(this.scenes.get(idx)));
               this.onClose();
               return true;
            }
         }

         if (this.page > 0 && this.isInside(event.x(), event.y(), this.leftArrowX, this.leftArrowY, 22, 30)) {
            this.page--;
            return true;
         }

         if (this.page < this.maxPage() && this.isInside(event.x(), event.y(), this.rightArrowX, this.rightArrowY, 22, 30)) {
            this.page++;
            return true;
         }
      }

      return super.mouseClicked(event, onPress);
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (event.key() == 263 && this.page > 0) {
         this.page--;
         return true;
      } else if (event.key() == 262 && this.page < this.maxPage()) {
         this.page++;
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   private boolean isInside(double mx, double my, int x, int y, int w, int h) {
      return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
