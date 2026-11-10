package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;
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
         return InputUtil.isKeyPressed(UMinecraft.getWindowHandle(), key);
      }
   }

   public static boolean isCtrlKeyDown() {
      return Screen.hasControlDown();
   }

   public static boolean isShiftKeyDown() {
      return Screen.hasShiftDown();
   }

   public static boolean isAltKeyDown() {
      return Screen.hasAltDown();
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
         return InputUtil.fromKeyCode(keyCode, scanCode).getLocalizedText().getString();
      }
   }
}
