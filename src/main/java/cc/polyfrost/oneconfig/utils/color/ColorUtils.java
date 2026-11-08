package cc.polyfrost.oneconfig.utils.color;

public final class ColorUtils {
   public static int getRed(int color) {
      return color >> 16 & 0xFF;
   }

   public static int getGreen(int color) {
      return color >> 8 & 0xFF;
   }

   public static int getBlue(int color) {
      return color & 0xFF;
   }

   public static int getAlpha(int color) {
      return color >> 24 & 0xFF;
   }

   public static int getColor(float red, float green, float blue, float alpha) {
      return getColor((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
   }

   public static int getColor(int red, int green, int blue) {
      return getColor(red, green, blue, 255);
   }

   public static int getColor(int red, int green, int blue, int alpha) {
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   public static int getColor(byte red, byte green, byte blue, byte alpha) {
      return getColor(red & 0xFF, green & 0xFF, blue & 0xFF, alpha & 0xFF);
   }

   public static int setRed(int color, int red) {
      return color & 65535 | red << 16;
   }

   public static int setGreen(int color, int green) {
      return color & 16711935 | green << 8;
   }

   public static int setBlue(int color, int blue) {
      return color & 16776960 | blue;
   }

   public static int setAlpha(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }
}
