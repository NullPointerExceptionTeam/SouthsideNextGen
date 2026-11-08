package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_638;

public class UMinecraft {
   public static class_310 getMinecraft() {
      return class_310.method_1551();
   }

   public static class_638 getWorld() {
      return getMinecraft().field_1687;
   }

   public static void setGuiScaleValue(int value) {
      getSettings().method_42474().method_41748(value);
   }

   public static class_315 getSettings() {
      return getMinecraft().field_1690;
   }

   public static int getGuiScale() {
      return getGuiScaleValue();
   }

   private static int getGuiScaleValue() {
      return (Integer)getSettings().method_42474().method_41753();
   }

   public static void setGuiScale(int value) {
      setGuiScaleValue(value);
   }

   public static long getWindowHandle() {
      return getMinecraft().method_22683().method_4490();
   }
}
