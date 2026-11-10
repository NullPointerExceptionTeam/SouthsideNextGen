package cc.polyfrost.oneconfig.libs.universal;

public class UResolution {
   public static float getScaleFactor() {
      return (float)UMinecraft.getMinecraft().getWindow().getScaleFactor();
   }

   public static final int getScaledWidth() {
      return UMinecraft.getMinecraft().getWindow().getScaledWidth();
   }

   public static final int getScaledHeight() {
      return UMinecraft.getMinecraft().getWindow().getScaledHeight();
   }

   public static final int getWindowWidth() {
      return UMinecraft.getMinecraft().getWindow().getFramebufferWidth();
   }

   public static final int getWindowHeight() {
      return UMinecraft.getMinecraft().getWindow().getFramebufferHeight();
   }

   public static final int getViewportWidth() {
      return UMinecraft.getMinecraft().getWindow().getFramebufferWidth();
   }

   public static final int getViewportHeight() {
      return UMinecraft.getMinecraft().getWindow().getFramebufferHeight();
   }
}
