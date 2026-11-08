package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import cc.polyfrost.oneconfig.platform.I18nPlatform;
import net.minecraft.class_1074;

public class I18nPlatformImpl implements I18nPlatform {
   @Override
   public String format(String key, Object... args) {
      return class_1074.method_4662(key, args);
   }

   @Override
   public String getKeyName(int key, int scanCode) {
      if (key <= -96) {
         int mouseButton = key + 100;

         return switch (mouseButton) {
            case 0 -> "Mouse Left";
            case 1 -> "Mouse Right";
            case 2 -> "Mouse Middle";
            default -> "Mouse " + (mouseButton + 1);
         };
      } else {
         String s = UKeyboard.getKeyName(key, scanCode);
         return s == null ? "Unknown" : s;
      }
   }
}
