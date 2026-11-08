package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

public final class UKeyboard {
   private static final int MOUSE_OFFSET = -100;

   public static boolean isMouseButton(int key) {
      return key <= -98 && key >= -100;
   }

   public static boolean isMouseCode(int key) {
      return key <= -80 && key <= -96;
   }

   public static boolean isKeyDown(int key) {
      if (key == 0 || key == -1) {
         return false;
      } else if (key <= -96) {
         int mouseButton = key - -100;
         return GLFW.glfwGetMouseButton(UMinecraft.getWindowHandle(), mouseButton) == 1;
      } else {
         return class_3675.method_15987(UMinecraft.getWindowHandle(), key);
      }
   }

   public static boolean isCtrlKeyDown() {
      return class_437.method_25441();
   }

   public static boolean isShiftKeyDown() {
      return class_437.method_25442();
   }

   public static boolean isAltKeyDown() {
      return class_437.method_25443();
   }

   public static boolean isKeyComboCtrlA(int key) {
      return key == 65 && isCtrlKeyDown() && !isShiftKeyDown() ? !isAltKeyDown() : false;
   }

   public static boolean isKeyComboCtrlC(int key) {
      return key == 67 && isCtrlKeyDown() && !isShiftKeyDown() ? !isAltKeyDown() : false;
   }

   public static boolean isKeyComboCtrlV(int key) {
      return key == 86 && isCtrlKeyDown() && !isShiftKeyDown() ? !isAltKeyDown() : false;
   }

   public static boolean isKeyComboCtrlX(int key) {
      return key == 88 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static final boolean isKeyComboCtrlY(int key) {
      return key == 89 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static boolean isKeyComboCtrlZ(int key) {
      return key == 90 && isCtrlKeyDown() && !isShiftKeyDown() ? !isAltKeyDown() : false;
   }

   public static boolean isKeyComboCtrlShiftZ(int key) {
      return key == 90 && isCtrlKeyDown() && isShiftKeyDown() && !isAltKeyDown();
   }

   public static String getKeyName(int keyCode, int scanCode) {
      if (keyCode <= -96) {
         int mouseButton = keyCode + 100;

         return switch (mouseButton) {
            case 0 -> "Mouse Left";
            case 1 -> "Mouse Right";
            case 2 -> "Mouse Middle";
            default -> "Mouse " + (mouseButton + 1);
         };
      } else {
         return class_3675.method_15985(keyCode, scanCode).method_27445().getString();
      }
   }
}
