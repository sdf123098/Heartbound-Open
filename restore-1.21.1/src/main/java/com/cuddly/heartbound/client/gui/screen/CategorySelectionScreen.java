package com.cuddly.heartbound.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class CategorySelectionScreen extends Screen {
   private static final int BTN_W = 120;
   private static final int BTN_H = 40;
   private static final int BTN_GAP = 30;
   private int panelX;
   private int panelY;
   private int panelW;
   private int panelH;
   private int defaultBtnX;
   private int defaultBtnY;
   private int customBtnX;
   private int customBtnY;

   public CategorySelectionScreen() {
      super(Text.translatable("gui.heartbound.girl_selection"));
   }

   @Override
   protected void init() {
      super.init();
      this.panelW = 330;
      this.panelH = 120;
      this.panelX = (this.width - this.panelW) / 2;
      this.panelY = (this.height - this.panelH) / 2;
      int btnAreaW = 270;
      int btnStartX = this.panelX + (this.panelW - btnAreaW) / 2;
      int btnY = this.panelY + 40;
      this.defaultBtnX = btnStartX;
      this.defaultBtnY = btnY;
      this.customBtnX = btnStartX + 120 + 30;
      this.customBtnY = btnY;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      ScreenDecorations.drawBackground(context, this.width, this.height);
      ScreenDecorations.drawPanel(context, this.panelX, this.panelY, this.panelW, this.panelH);
      ScreenDecorations.drawTitleWithDecor(context, this.textRenderer, this.title, this.width / 2, this.panelY + 12);
      ScreenDecorations.drawButton(
         context, this.textRenderer, this.defaultBtnX, this.defaultBtnY, 120, 40, Text.translatable("gui.heartbound.button.defaultGirl"), mouseX, mouseY, true
      );
      ScreenDecorations.drawButton(
         context, this.textRenderer, this.customBtnX, this.customBtnY, 120, 40, Text.translatable("gui.heartbound.button.customGirl"), mouseX, mouseY, true
      );
      super.render(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (this.isInside(mouseX, mouseY, this.defaultBtnX, this.defaultBtnY, 120, 40)) {
            MinecraftClient.getInstance().setScreen(new GirlSelectionScreen(false));
            return true;
         }

         if (this.isInside(mouseX, mouseY, this.customBtnX, this.customBtnY, 120, 40)) {
            MinecraftClient.getInstance().setScreen(new GirlSelectionScreen(true));
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
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
