package dev.southside.utils.render;

import java.awt.Color;
import net.minecraft.util.math.MathHelper;

public class ColorUtils {
   public static Color getHealthColor(float health, float maxHealth) {
      float clampedHealth = health * (20.0F / maxHealth);
      float[] fractions = new float[]{0.0F, 0.3F, 0.7F, 1.0F};
      Color[] colors = new Color[]{Color.RED, Color.RED, Color.YELLOW, Color.GREEN};
      float progress = MathHelper.clamp(clampedHealth / 20.0F, 0.0F, 1.0F);
      return blendColors(fractions, colors, progress).brighter();
   }

   public static Color injectAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
   }

   public static Color blendColors(float[] fractions, Color[] colors, float progress) {
      if (fractions.length == colors.length) {
         int[] indices = getFractionIndices(fractions, progress);
         float[] range = new float[]{fractions[indices[0]], fractions[indices[1]]};
         Color[] colorRange = new Color[]{colors[indices[0]], colors[indices[1]]};
         float max = range[1] - range[0];
         float value = progress - range[0];
         float weight = value / max;
         return blend(colorRange[0], colorRange[1], 1.0F - weight);
      } else {
         throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
      }
   }

   public static int[] getFractionIndices(float[] fractions, float progress) {
      int[] range = new int[2];
      int startPoint = 0;

      while (startPoint < fractions.length && fractions[startPoint] <= progress) {
         startPoint++;
      }

      if (startPoint >= fractions.length) {
         startPoint = fractions.length - 1;
      }

      range[0] = startPoint - 1;
      range[1] = startPoint;
      return range;
   }

   public static int applyOpacity(int color, float opacity) {
      Color old = new Color(color);
      return applyOpacity(old, opacity).getRGB();
   }

   public static Color applyOpacity(Color color, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity));
   }

   public static Color blend(Color color1, Color color2, float ratio) {
      float ir = 1.0F - ratio;
      float[] rgb1 = color1.getColorComponents(null);
      float[] rgb2 = color2.getColorComponents(null);
      float red = rgb1[0] * ratio + rgb2[0] * ir;
      float green = rgb1[1] * ratio + rgb2[1] * ir;
      float blue = rgb1[2] * ratio + rgb2[2] * ir;
      red = Math.min(255.0F, Math.max(0.0F, red));
      green = Math.min(255.0F, Math.max(0.0F, green));
      blue = Math.min(255.0F, Math.max(0.0F, blue));
      return new Color(red, green, blue);
   }
}
