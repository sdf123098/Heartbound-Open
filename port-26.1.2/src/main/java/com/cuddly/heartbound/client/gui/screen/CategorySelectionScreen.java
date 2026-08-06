package com.cuddly.heartbound.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
      super(Component.translatable("gui.heartbound.girl_selection"));
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
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      ScreenDecorations.drawBackground(context, this.width, this.height);
      ScreenDecorations.drawPanel(context, this.panelX, this.panelY, this.panelW, this.panelH);
      ScreenDecorations.drawTitleWithDecor(context, this.font, this.title, this.width / 2, this.panelY + 12);
      ScreenDecorations.drawButton(
         context, this.font, this.defaultBtnX, this.defaultBtnY, 120, 40, Component.translatable("gui.heartbound.button.defaultGirl"), mouseX, mouseY, true
      );
      ScreenDecorations.drawButton(
         context, this.font, this.customBtnX, this.customBtnY, 120, 40, Component.translatable("gui.heartbound.button.customGirl"), mouseX, mouseY, true
      );
      super.extractRenderState(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean onPress) {
      if (event.button() == 0) {
         if (this.isInside(event.x(), event.y(), this.defaultBtnX, this.defaultBtnY, 120, 40)) {
            Minecraft.getInstance().setScreen(new GirlSelectionScreen(false));
            return true;
         }

         if (this.isInside(event.x(), event.y(), this.customBtnX, this.customBtnY, 120, 40)) {
            Minecraft.getInstance().setScreen(new GirlSelectionScreen(true));
            return true;
         }
      }

      return super.mouseClicked(event, onPress);
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
