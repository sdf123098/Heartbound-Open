package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class WarmTextFieldWidget extends EditBox {
   private final Font tr;
   private long focusTime = Util.getMillis();
   private int trackedSelectionEnd;
   private Component placeholderText;

   public WarmTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text) {
      super(textRenderer, x, y, width, height, text);
      this.tr = textRenderer;
   }

   @Override
   public void setHint(Component placeholder) {
      super.setHint(placeholder);
      this.placeholderText = placeholder;
   }

   @Override
   public void setFocused(boolean focused) {
      super.setFocused(focused);
      if (focused) {
         this.focusTime = Util.getMillis();
      }
   }

   @Override
   public void setHighlightPos(int index) {
      super.setHighlightPos(index);
      this.trackedSelectionEnd = Mth.clamp(index, 0, this.getValue().length());
   }

   @Override
   public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
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
         String fullText = this.getValue();
         String visibleText = this.tr.plainSubstrByWidth(fullText, innerWidth);
         int cursor = this.getCursorPosition();
         int clampedCursor = Mth.clamp(cursor, 0, visibleText.length());
         if (!visibleText.isEmpty()) {
            context.text(this.tr, visibleText, textX, textY, -11913432, false);
         } else if (!this.isFocused() && this.placeholderText != null) {
            String placeholder = this.tr.plainSubstrByWidth(this.placeholderText.getString(), innerWidth);
            context.text(this.tr, placeholder, textX, textY, -6649222, false);
         }

         boolean cursorVisible = this.isFocused()
            && (Util.getMillis() - this.focusTime) / 300L % 2L == 0L
            && clampedCursor >= 0
            && clampedCursor <= visibleText.length();
         if (cursorVisible) {
            int cursorX = textX + this.tr.width(visibleText.substring(0, clampedCursor));
            if (cursor >= fullText.length()) {
               context.text(this.tr, "_", cursorX, textY, -11913432, false);
            } else {
               context.fill(cursorX, textY - 1, cursorX + 1, textY + 1 + 9, -3092272);
            }
         }

         if (this.isFocused() && this.trackedSelectionEnd != cursor) {
            int selEndClamped = Mth.clamp(this.trackedSelectionEnd, 0, visibleText.length());
            int sx1 = textX + this.tr.width(visibleText.substring(0, Math.min(clampedCursor, selEndClamped)));
            int sx2 = textX + this.tr.width(visibleText.substring(0, Math.max(clampedCursor, selEndClamped)));
            if (sx1 != sx2) {
               context.fill(sx1, textY - 1, sx2, textY + 1 + 9, -16776961);
            }
         }
      }
   }
}
