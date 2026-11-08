package dev.southside.utils.misc;

public class ClickUtil {
   public static boolean isHovering(double mouseX, double mouseY, int x, int y, int x1, int y1) {
      return mouseX > x && mouseX < x1 && mouseY > y && mouseY < y1;
   }

   public static boolean isHovering(double mouseX, double mouseY, float x, float y, float x1, float y1) {
      return mouseX > x && mouseX < x1 && mouseY > y && mouseY < y1;
   }
}
