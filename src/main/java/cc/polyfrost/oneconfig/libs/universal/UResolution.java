package cc.polyfrost.oneconfig.libs.universal;

public class UResolution {
   public static float getScaleFactor() {
      return (float)UMinecraft.getMinecraft().method_22683().method_4495();
   }

   public static final int getScaledWidth() {
      return UMinecraft.getMinecraft().method_22683().method_4486();
   }

   public static final int getScaledHeight() {
      return UMinecraft.getMinecraft().method_22683().method_4502();
   }

   public static final int getWindowWidth() {
      return UMinecraft.getMinecraft().method_22683().method_4489();
   }

   public static final int getWindowHeight() {
      return UMinecraft.getMinecraft().method_22683().method_4506();
   }

   public static final int getViewportWidth() {
      return UMinecraft.getMinecraft().method_22683().method_4489();
   }

   public static final int getViewportHeight() {
      return UMinecraft.getMinecraft().method_22683().method_4506();
   }
}
