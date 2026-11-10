package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;

public class UMinecraft {
   public static MinecraftClient getMinecraft() {
      return MinecraftClient.getInstance();
   }

   public static ClientWorld getWorld() {
      return getMinecraft().world;
   }

   public static void setGuiScaleValue(int value) {
      getSettings().getGuiScale().setValue(value);
   }

   public static GameOptions getSettings() {
      return getMinecraft().options;
   }

   public static int getGuiScale() {
      return getGuiScaleValue();
   }

   private static int getGuiScaleValue() {
      return (Integer)getSettings().getGuiScale().getValue();
   }

   public static void setGuiScale(int value) {
      setGuiScaleValue(value);
   }

   public static long getWindowHandle() {
      return getMinecraft().getWindow().getHandle();
   }
}
