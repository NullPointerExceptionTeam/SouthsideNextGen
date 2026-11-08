package cc.polyfrost.oneconfig.utils.color;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

public class ColorUtil {
   private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
   private static final long startTime = System.currentTimeMillis();
   public static final int[] hexColors = new int[16];

   public static Color tripleColor(int rgbValue) {
      return tripleColor(rgbValue, 1.0F);
   }

   public static Color tripleColor(int rgbValue, float alpha) {
      alpha = Math.min(1.0F, Math.max(0.0F, alpha));
      return new Color(rgbValue, rgbValue, rgbValue, (int)(255.0F * alpha));
   }

   public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
      return interpolate(oldValue, newValue, (float)interpolationValue).floatValue();
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return interpolate(oldValue, newValue, (float)interpolationValue).intValue();
   }

   public static Color[] getAnalogousColor(Color color) {
      Color[] colors = new Color[2];
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      float degree = 0.083333336F;
      float newHueAdded = hsb[0] + degree;
      colors[0] = new Color(Color.HSBtoRGB(newHueAdded, hsb[1], hsb[2]));
      float newHueSubtracted = hsb[0] - degree;
      colors[1] = new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
      return colors;
   }

   public static int withAlpha(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }

   public static Color withAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   public static Color getRandomColor() {
      return new Color(Color.HSBtoRGB((float)Math.random(), (float)(0.5 + Math.random() / 2.0), (float)(0.5 + Math.random() / 2.0)));
   }

   public static Color hslToRGB(float[] hsl) {
      float red;
      float green;
      float blue;
      if (hsl[1] == 0.0F) {
         blue = 1.0F;
         green = 1.0F;
         red = 1.0F;
      } else {
         float q = hsl[2] < 0.5 ? hsl[2] * (1.0F + hsl[1]) : hsl[2] + hsl[1] - hsl[2] * hsl[1];
         float p = 2.0F * hsl[2] - q;
         red = hueToRGB(p, q, hsl[0] + 0.33333334F);
         green = hueToRGB(p, q, hsl[0]);
         blue = hueToRGB(p, q, hsl[0] - 0.33333334F);
      }

      red *= 255.0F;
      green *= 255.0F;
      blue *= 255.0F;
      return new Color((int)red, (int)green, (int)blue);
   }

   public static float hueToRGB(float p, float q, float t) {
      float newT = t;
      if (t < 0.0F) {
         newT = t + 1.0F;
      }

      if (newT > 1.0F) {
         newT--;
      }

      if (newT < 0.16666667F) {
         return p + (q - p) * 6.0F * newT;
      } else if (newT < 0.5F) {
         return q;
      } else {
         return newT < 0.6666667F ? p + (q - p) * (0.6666667F - newT) * 6.0F : p;
      }
   }

   public static float[] rgbToHSL(Color rgb) {
      float red = rgb.getRed() / 255.0F;
      float green = rgb.getGreen() / 255.0F;
      float blue = rgb.getBlue() / 255.0F;
      float max = Math.max(Math.max(red, green), blue);
      float min = Math.min(Math.min(red, green), blue);
      float c = (max + min) / 2.0F;
      float[] hsl = new float[]{c, c, c};
      if (max == min) {
         hsl[0] = hsl[1] = 0.0F;
      } else {
         float d = max - min;
         hsl[1] = hsl[2] > 0.5 ? d / (2.0F - max - min) : d / (max + min);
         if (max == red) {
            hsl[0] = (green - blue) / d + (green < blue ? 6 : 0);
         } else if (max == blue) {
            hsl[0] = (blue - red) / d + 2.0F;
         } else if (max == green) {
            hsl[0] = (red - green) / d + 4.0F;
         }

         hsl[0] /= 6.0F;
      }

      return hsl;
   }

   public static Color imitateTransparency(Color backgroundColor, Color accentColor, float percentage) {
      return new Color(interpolateColor(backgroundColor, accentColor, 255.0F * percentage / 255.0F));
   }

   public static int applyOpacity(int color, float opacity) {
      Color old = new Color(color);
      return applyOpacity(old, opacity).getRGB();
   }

   public static Color applyOpacity(Color color, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity));
   }

   public static Color darker(Color color, float FACTOR) {
      return new Color(
         Math.max((int)(color.getRed() * FACTOR), 0),
         Math.max((int)(color.getGreen() * FACTOR), 0),
         Math.max((int)(color.getBlue() * FACTOR), 0),
         color.getAlpha()
      );
   }

   public static Color brighter(Color color, float FACTOR) {
      int r = color.getRed();
      int g = color.getGreen();
      int b = color.getBlue();
      int alpha = color.getAlpha();
      int i = (int)(1.0 / (1.0 - FACTOR));
      if (r == 0 && g == 0 && b == 0) {
         return new Color(i, i, i, alpha);
      } else {
         if (r > 0 && r < i) {
            r = i;
         }

         if (g > 0 && g < i) {
            g = i;
         }

         if (b > 0 && b < i) {
            b = i;
         }

         return new Color(Math.min((int)(r / FACTOR), 255), Math.min((int)(g / FACTOR), 255), Math.min((int)(b / FACTOR), 255), alpha);
      }
   }

   public static Color averageColor(BufferedImage bi, int width, int height, int pixelStep) {
      int[] color = new int[3];
      int x = 0;

      while (x < width) {
         for (int y = 0; y < height; y += pixelStep) {
            Color pixel = new Color(bi.getRGB(x, y));
            color[0] += pixel.getRed();
            color[1] += pixel.getGreen();
            color[2] += pixel.getBlue();
         }

         x += pixelStep;
      }

      x = width * height / (pixelStep * pixelStep);
      return new Color(color[0] / x, color[1] / x, color[2] / x);
   }

   public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      float hue = angle / 360.0F;
      Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int)(opacity * 255.0F))));
   }

   public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = (angle >= 180 ? 360 - angle : angle) * 2;
      return trueColor ? interpolateColorHue(start, end, angle / 360.0F) : interpolateColorC(start, end, angle / 360.0F);
   }

   public static int interpolateColor(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      return interpolateColorC(color1, color2, amount).getRGB();
   }

   public static int interpolateColor(int color1, int color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      Color cColor1 = new Color(color1);
      Color cColor2 = new Color(color2);
      return interpolateColorC(cColor1, cColor2, amount).getRGB();
   }

   public static Color interpolateColorC(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      return new Color(
         interpolateInt(color1.getRed(), color2.getRed(), amount),
         interpolateInt(color1.getGreen(), color2.getGreen(), amount),
         interpolateInt(color1.getBlue(), color2.getBlue(), amount),
         interpolateInt(color1.getAlpha(), color2.getAlpha(), amount)
      );
   }

   public static Color interpolateColorHue(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
      float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
      Color resultColor = Color.getHSBColor(
         interpolateFloat(color1HSB[0], color2HSB[0], amount),
         interpolateFloat(color1HSB[1], color2HSB[1], amount),
         interpolateFloat(color1HSB[2], color2HSB[2], amount)
      );
      return applyOpacity(resultColor, interpolateInt(color1.getAlpha(), color2.getAlpha(), amount) / 255.0F);
   }

   public static Color fade(int speed, int index, Color color, float alpha) {
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360.0F));
      return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int)(alpha * 255.0F))));
   }

   private static float getAnimationEquation(int index, int speed) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      return ((angle > 180 ? 360 - angle : angle) + 180) / 360.0F;
   }

   public static int[] createColorArray(int color) {
      return new int[]{bitChangeColor(color, 16), bitChangeColor(color, 8), bitChangeColor(color, 0), bitChangeColor(color, 24)};
   }

   public static int getOppositeColor(int color) {
      int R = bitChangeColor(color, 0);
      int G = bitChangeColor(color, 8);
      int B = bitChangeColor(color, 16);
      int A = bitChangeColor(color, 24);
      R = 255 - R;
      G = 255 - G;
      B = 255 - B;
      return R + (G << 8) + (B << 16) + (A << 24);
   }

   public static Color getOppositeColor(Color color) {
      return new Color(getOppositeColor(color.getRGB()));
   }

   private static int bitChangeColor(int color, int bitChange) {
      return color >> bitChange & 0xFF;
   }

   public static String stripColor(String input) {
      return COLOR_PATTERN.matcher(input).replaceAll("");
   }

   public static String translateAlternateColorCodes(String textToTranslate) {
      char[] chars = textToTranslate.toCharArray();

      for (int i = 0; i < chars.length - 1; i++) {
         if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(String.valueOf(chars[i + 1]).toLowerCase())) {
            chars[i] = 167;
            chars[i + 1] = Character.toLowerCase(chars[i + 1]);
         }
      }

      return new String(chars);
   }

   public static Color colorCode(String code, int alpha) {
      String var2 = code.toLowerCase();

      return switch (var2) {
         case "0" -> new Color(0, 0, 0, alpha);
         case "1" -> new Color(0, 0, 170, alpha);
         case "2" -> new Color(0, 170, 0, alpha);
         case "3" -> new Color(0, 170, 170, alpha);
         case "4" -> new Color(170, 0, 0, alpha);
         case "5" -> new Color(170, 0, 170, alpha);
         case "6" -> new Color(255, 170, 0, alpha);
         case "7" -> new Color(170, 170, 170, alpha);
         case "8" -> new Color(85, 85, 85, alpha);
         case "9" -> new Color(85, 85, 255, alpha);
         case "a" -> new Color(85, 255, 85, alpha);
         case "b" -> new Color(85, 255, 255, alpha);
         case "c" -> new Color(255, 85, 85, alpha);
         case "d" -> new Color(255, 85, 255, alpha);
         case "e" -> new Color(255, 255, 85, alpha);
         default -> new Color(255, 255, 255, alpha);
      };
   }

   public static int overwriteAlpha(int colour, int alphaComponent) {
      int red = colour >> 16 & 0xFF;
      int green = colour >> 8 & 0xFF;
      int blue = colour & 0xFF;
      return (alphaComponent & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
   }

   public static Color slowlyRainbow(long time, int count, float qd, float sq) {
      Color color = new Color(Color.HSBtoRGB(((float)time + count * -3000000.0F) / 2.0F / 1.0E9F, qd, sq));
      return new Color(color.getRed() / 255.0F * 1.0F, color.getGreen() / 255.0F * 1.0F, color.getBlue() / 255.0F * 1.0F, color.getAlpha() / 255.0F);
   }

   public static Color skyRainbow(int var2, float bright, float st, double speed) {
      long v1 = (long)Math.ceil(System.currentTimeMillis() / speed + var2 * 109L) / 5L;
      float hue = 360.0F % (float)v1 / 360.0F < 0.5 ? -((float)v1 / 360.0F) : (float)v1 / 360.0F;
      return Color.getHSBColor(hue, st, bright);
   }

   public static Color fade(Color color, int index, int count) {
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      float brightness = Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0F + (float)index / count * 2.0F) % 2.0F - 1.0F);
      brightness = 0.5F + 0.5F * brightness;
      hsb[2] = brightness % 2.0F;
      return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
   }

   public static Color reverseColor(Color color) {
      return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
   }

   public static Color healthColor(float hp, float maxHP, int alpha) {
      int pct = (int)(hp / maxHP * 255.0F);
      return new Color(Math.max(Math.min(255 - pct, 255), 0), Math.max(Math.min(pct, 255), 0), 0, alpha);
   }

   public static Color mixColors(Color color1, Color color2, float percent) {
      return new Color(
         color1.getRed() + (int)((color2.getRed() - color1.getRed()) * percent),
         color1.getGreen() + (int)((color2.getGreen() - color1.getGreen()) * percent),
         color1.getBlue() + (int)((color2.getBlue() - color1.getBlue()) * percent),
         color1.getAlpha() + (int)((color2.getAlpha() - color1.getAlpha()) * percent)
      );
   }

   public static int toRGB(int n, int n2, int n3, int n4) {
      return (n4 & 0xFF) << 24 | (n3 & 0xFF) << 16 | (n2 & 0xFF) << 8 | n & 0xFF;
   }

   public static int toRGB(float f, float f2, float f3, float f4) {
      return toRGB((int)(f * 255.0F), (int)(f2 * 255.0F), (int)(f3 * 255.0F), (int)(f4 * 255.0F));
   }

   public static Color reAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   public static Color reAlpha(Color color, float alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(alpha * 255.0F));
   }

   static {
      for (int i = 0; i < 16; i++) {
         int baseColor = (i >> 3 & 1) * 85;
         int red = (i >> 2 & 1) * 170 + baseColor + (i == 6 ? 85 : 0);
         int green = (i >> 1 & 1) * 170 + baseColor;
         int blue = (i & 1) * 170 + baseColor;
         hexColors[i] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
      }
   }
}
