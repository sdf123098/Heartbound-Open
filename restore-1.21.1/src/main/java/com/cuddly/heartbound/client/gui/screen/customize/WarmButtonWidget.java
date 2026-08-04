package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.ScreenDecorations;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.text.Text;

public class WarmButtonWidget extends ButtonWidget {
   private final TextRenderer textRenderer;

   public WarmButtonWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message, PressAction onPress) {
      super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
      this.textRenderer = textRenderer;
   }

   @Override
   protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      Text label = this.getMessage();
      ScreenDecorations.drawButton(context, this.textRenderer, this.getX(), this.getY(), this.getWidth(), this.getHeight(), label, mouseX, mouseY, this.active);
   }
}
