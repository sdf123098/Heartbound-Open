package com.cuddly.heartbound.client.gui.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ScreenDecorations {
   private static final int BG_TOP = -1608511440;
   private static final int BG_BOTTOM = -1606410187;
   private static final int PANEL_TOP = -659996;
   private static final int PANEL_BOTTOM = -1517368;
   private static final int PANEL_BORDER = -3889014;
   private static final int PANEL_INNER_HIGHLIGHT = 553648127;
   private static final int PANEL_INNER_SHADOW = 268435456;
   private static final int ORNAMENT_COLOR = -2840470;
   private static final int ORNAMENT_LEN = 8;
   private static final int ORNAMENT_THICK = 2;
   private static final int TITLE_LINE_COLOR = -2840470;
   private static final int TITLE_LINE_LEN = 20;
   private static final int TITLE_LINE_GAP = 6;
   private static final int BTN_TOP = -1781580;
   private static final int BTN_BOTTOM = -2836328;
   private static final int BTN_HOVER_TOP = -992060;
   private static final int BTN_HOVER_BOTTOM = -2045780;
   private static final int BTN_DISABLED_TOP = -2831164;
   private static final int BTN_DISABLED_BOTTOM = -3884884;
   private static final int BTN_BORDER = -4679568;
   private static final int BTN_BORDER_HOVER = -6258608;
   private static final int TEXT_DARK = -11913432;
   private static final int TEXT_DISABLED = -6649222;

   private ScreenDecorations() {
   }

   public static void drawBackground(GuiGraphicsExtractor ctx, int width, int height) {
      ctx.fillGradient(0, 0, width, height, -1608511440, -1606410187);
   }

   public static void drawPanel(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
      ctx.fillGradient(x, y, x + w, y + h, -659996, -1517368);
      ctx.fill(x + 1, y + 1, x + w - 1, y + 2, 553648127);
      ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 268435456);
      outlineRect(ctx, x, y, w, h, -3889014);
      drawCornerOrnaments(ctx, x, y, w, h);
   }

   public static void drawCornerOrnaments(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
      int c = -2840470;
      int len = 8;
      int t = 2;
      ctx.fill(x - 1, y - 1, x - 1 + len, y - 1 + t, c);
      ctx.fill(x - 1, y - 1, x - 1 + t, y - 1 + len, c);
      ctx.fill(x + w + 1 - len, y - 1, x + w + 1, y - 1 + t, c);
      ctx.fill(x + w + 1 - t, y - 1, x + w + 1, y - 1 + len, c);
      ctx.fill(x - 1, y + h + 1 - t, x - 1 + len, y + h + 1, c);
      ctx.fill(x - 1, y + h + 1 - len, x - 1 + t, y + h + 1, c);
      ctx.fill(x + w + 1 - len, y + h + 1 - t, x + w + 1, y + h + 1, c);
      ctx.fill(x + w + 1 - t, y + h + 1 - len, x + w + 1, y + h + 1, c);
   }

   public static void drawTitleWithDecor(GuiGraphicsExtractor ctx, Font tr, Component title, int centerX, int y) {
      int titleW = tr.width(title);
      int tx = centerX - titleW / 2;
      ctx.text(tr, title, tx, y, -11913432, false);
      int lineY = y + 9 / 2;
      int leftEnd = tx - 6;
      int rightStart = tx + titleW + 6;
      if (leftEnd - 20 > 0) {
         ctx.fill(leftEnd - 20, lineY, leftEnd, lineY + 1, -2840470);
      }

      ctx.fill(rightStart, lineY, rightStart + 20, lineY + 1, -2840470);
   }

   public static void drawButton(GuiGraphicsExtractor ctx, Font tr, int x, int y, int w, int h, Component label, int mx, int my, boolean active) {
      boolean hovered = active && mx >= x && mx < x + w && my >= y && my < y + h;
      int top;
      int bottom;
      if (!active) {
         top = -2831164;
         bottom = -3884884;
      } else if (hovered) {
         top = -992060;
         bottom = -2045780;
      } else {
         top = -1781580;
         bottom = -2836328;
      }

      int border = hovered ? -6258608 : -4679568;
      int textColor = active ? -11913432 : -6649222;
      ctx.fillGradient(x, y, x + w, y + h, top, bottom);
      if (active) {
         ctx.fill(x + 1, y + 1, x + w - 1, y + 2, 822083583);
         ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 536870912);
      }

      outlineRect(ctx, x, y, w, h, border);
      drawFittedText(ctx, tr, label, x, y, w, h, textColor);
   }

   public static void drawWarmButton(GuiGraphicsExtractor ctx, Font tr, int x, int y, int w, int h, Component label, int mx, int my, int textColor) {
      boolean hovered = mx >= x && mx < x + w && my >= y && my < y + h;
      int top = hovered ? -992060 : -1781580;
      int bottom = hovered ? -2045780 : -2836328;
      int border = hovered ? -6258608 : -4679568;
      ctx.fillGradient(x, y, x + w, y + h, top, bottom);
      ctx.fill(x + 1, y + 1, x + w - 1, y + 2, 822083583);
      ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 536870912);
      outlineRect(ctx, x, y, w, h, border);
      drawFittedText(ctx, tr, label, x, y, w, h, textColor);
   }

   private static void drawFittedText(GuiGraphicsExtractor ctx, Font tr, Component label, int x, int y, int w, int h, int color) {
      int tw = tr.width(label);
      int padding = 4;
      int maxW = w - padding * 2;
      int textY = y + (h - 9) / 2;
      if (tw > maxW && tw > 0) {
         int overflow = tw - maxW;
         long time = System.currentTimeMillis() % ((long)overflow * 40L + 2000L);
         int offset;
         if (time < 1000L) {
            offset = 0;
         } else if (time < 1000L + (long)overflow * 40L) {
            offset = (int)((time - 1000L) / 40L);
         } else {
            offset = overflow;
         }

         ctx.enableScissor(x + padding, y, x + w - padding, y + h);
         ctx.text(tr, label, x + padding - offset, textY, color, false);
         ctx.disableScissor();
      } else {
         ctx.text(tr, label, x + (w - tw) / 2, textY, color, false);
      }
   }

   public static void outlineRect(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
      ctx.fill(x, y, x + w, y + 1, color);
      ctx.fill(x, y + h - 1, x + w, y + h, color);
      ctx.fill(x, y + 1, x + 1, y + h - 1, color);
      ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
   }
}
