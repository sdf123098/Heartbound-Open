package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.networking.C2S.TransformStartSceneC2SPacket;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
      super(Text.translatable("gui.heartbound.sceneOptions"));
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
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      ScreenDecorations.drawBackground(context, this.width, this.height);
      ScreenDecorations.drawPanel(context, this.panelX, this.panelY, this.panelW, this.panelH);
      ScreenDecorations.drawTitleWithDecor(context, this.textRenderer, this.title, this.width / 2, this.panelY + 18);
      int startIdx = this.page * 6;

      for (int slot = 0; slot < 6; slot++) {
         int idx = startIdx + slot;
         int col = slot % 2;
         int row = slot / 2;
         int bx = this.gridX + col * 144;
         int by = this.gridY + row * 34;
         if (idx < this.scenes.size()) {
            Text label = Text.translatable(this.scenes.get(idx).displayName());
            ScreenDecorations.drawButton(context, this.textRenderer, bx, by, 130, 24, label, mouseX, mouseY, true);
         }
      }

      boolean canLeft = this.page > 0;
      boolean canRight = this.page < this.maxPage();
      ScreenDecorations.drawButton(context, this.textRenderer, this.leftArrowX, this.leftArrowY, 22, 30, Text.literal("◀"), mouseX, mouseY, canLeft);
      ScreenDecorations.drawButton(context, this.textRenderer, this.rightArrowX, this.rightArrowY, 22, 30, Text.literal("▶"), mouseX, mouseY, canRight);
      if (this.maxPage() > 0) {
         String pageText = this.page + 1 + "/" + (this.maxPage() + 1);
         int pw = this.textRenderer.getWidth(pageText);
         context.drawText(this.textRenderer, pageText, (this.width - pw) / 2, this.panelY + this.panelH - 18 + 2, -9741750, false);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
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
            if (mouseX >= (double)bx && mouseX < (double)(bx + 130) && mouseY >= (double)by && mouseY < (double)(by + 24)) {
               ClientPlayNetworking.send(new TransformStartSceneC2SPacket(this.scenes.get(idx)));
               this.close();
               return true;
            }
         }

         if (this.page > 0 && this.isInside(mouseX, mouseY, this.leftArrowX, this.leftArrowY, 22, 30)) {
            this.page--;
            return true;
         }

         if (this.page < this.maxPage() && this.isInside(mouseX, mouseY, this.rightArrowX, this.rightArrowY, 22, 30)) {
            this.page++;
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 263 && this.page > 0) {
         this.page--;
         return true;
      } else if (keyCode == 262 && this.page < this.maxPage()) {
         this.page++;
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private boolean isInside(double mx, double my, int x, int y, int w, int h) {
      return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   @Override
   public boolean shouldPause() {
      return false;
   }
}
