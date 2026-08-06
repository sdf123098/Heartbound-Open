package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.ScreenDecorations;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class WarmButtonWidget extends Button {
   private final Font textRenderer;

   public WarmButtonWidget(Font textRenderer, int x, int y, int width, int height, Component message, OnPress onPress) {
      super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
      this.textRenderer = textRenderer;
   }

   @Override
   protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      Component label = this.getMessage();
      ScreenDecorations.drawButton(context, this.textRenderer, this.getX(), this.getY(), this.getWidth(), this.getHeight(), label, mouseX, mouseY, this.active);
   }
}
