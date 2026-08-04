package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class WarmTextFieldWidget extends TextFieldWidget {
   private final TextRenderer tr;
   private long focusTime = Util.getMeasuringTimeMs();
   private int trackedSelectionEnd;
   private Text placeholderText;

   public WarmTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
      super(textRenderer, x, y, width, height, text);
      this.tr = textRenderer;
   }

   @Override
   public void setPlaceholder(Text placeholder) {
      super.setPlaceholder(placeholder);
      this.placeholderText = placeholder;
   }

   @Override
   public void setFocused(boolean focused) {
      super.setFocused(focused);
      if (focused) {
         this.focusTime = Util.getMeasuringTimeMs();
      }
   }

   @Override
   public void setSelectionEnd(int index) {
      super.setSelectionEnd(index);
      this.trackedSelectionEnd = MathHelper.clamp(index, 0, this.getText().length());
   }

   @Override
   public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         int x = this.getX();
         int y = this.getY();
         int w = this.getWidth();
         int h = this.getHeight();
         context.fill(x, y, x + w, y + h, -659996);
         int borderColor = this.isFocused() ? -4679568 : -3889014;
         CustomizeScreen.outlineRect(context, x, y, w, h, borderColor);
         int textX = x + 4;
         int textY = y + (h - 8) / 2;
         int innerWidth = w - 8;
         String fullText = this.getText();
         String visibleText = this.tr.trimToWidth(fullText, innerWidth);
         int cursor = this.getCursor();
         int clampedCursor = MathHelper.clamp(cursor, 0, visibleText.length());
         if (!visibleText.isEmpty()) {
            context.drawText(this.tr, visibleText, textX, textY, -11913432, false);
         } else if (!this.isFocused() && this.placeholderText != null) {
            String placeholder = this.tr.trimToWidth(this.placeholderText.getString(), innerWidth);
            context.drawText(this.tr, placeholder, textX, textY, -6649222, false);
         }

         boolean cursorVisible = this.isFocused()
            && (Util.getMeasuringTimeMs() - this.focusTime) / 300L % 2L == 0L
            && clampedCursor >= 0
            && clampedCursor <= visibleText.length();
         if (cursorVisible) {
            int cursorX = textX + this.tr.getWidth(visibleText.substring(0, clampedCursor));
            if (cursor >= fullText.length()) {
               context.drawText(this.tr, "_", cursorX, textY, -11913432, false);
            } else {
               context.fill(RenderLayer.getGuiOverlay(), cursorX, textY - 1, cursorX + 1, textY + 1 + 9, -3092272);
            }
         }

         if (this.isFocused() && this.trackedSelectionEnd != cursor) {
            int selEndClamped = MathHelper.clamp(this.trackedSelectionEnd, 0, visibleText.length());
            int sx1 = textX + this.tr.getWidth(visibleText.substring(0, Math.min(clampedCursor, selEndClamped)));
            int sx2 = textX + this.tr.getWidth(visibleText.substring(0, Math.max(clampedCursor, selEndClamped)));
            if (sx1 != sx2) {
               context.fill(RenderLayer.getGuiTextHighlight(), sx1, textY - 1, sx2, textY + 1 + 9, -16776961);
            }
         }
      }
   }
}
