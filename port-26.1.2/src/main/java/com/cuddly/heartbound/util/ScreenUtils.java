package com.cuddly.heartbound.util;

public class ScreenUtils {
   public static boolean isMouseOverHere(double mouseX, double mouseY, int x, int y, int width, int height) {
      return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
   }
}
