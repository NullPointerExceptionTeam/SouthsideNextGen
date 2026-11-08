package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.module.Category;
import dev.southside.module.Module;
import java.awt.Color;

public class Theme extends Module {
   public static Dropdown colorMode = new Dropdown("Color", "Sky", "Sky");
   public static Slider colorSpeed = new Slider("Color Speed", 18, 1, 54, 1);

   public Theme() {
      super(Category.Client, "Theme", "主题");
   }

   @Override
   public void onEnable() {
      this.setEnable(false);
   }

   public static Color getColor(int count) {
      String var1 = colorMode.getMode();
      byte var2 = -1;
      switch (var1.hashCode()) {
         case 83201:
            if (var1.equals("Sky")) {
               var2 = 0;
            }
         default:
            return switch (var2) {
               case 0 -> skyRainbow(colorSpeed.getValue().intValue(), count);
               default -> Color.WHITE;
            };
      }
   }

   private static Color skyRainbow(int speed, int index) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      int var3;
      return Color.getHSBColor((float)((var3 = angle % 360) / 360.0) < 0.5 ? -((float)(var3 / 360.0)) : (float)(var3 / 360.0), 0.5F, 1.0F);
   }
}
