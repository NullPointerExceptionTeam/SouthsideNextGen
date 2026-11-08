package cc.polyfrost.oneconfig.utils.color;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import java.awt.Color;

public class ColorPalette {
   public static final ColorPalette TRANSPARENT = new ColorPalette("Transparent", Colors.TRANSPARENT, Colors.TRANSPARENT, Colors.TRANSPARENT);
   public static final ColorPalette PRIMARY = new ColorPalette("Primary", Colors.PRIMARY_600, Colors.PRIMARY_700, Colors.PRIMARY_700_80);
   public static final ColorPalette SECONDARY = new ColorPalette("Secondary", Colors.GRAY_500, Colors.GRAY_400, Colors.GRAY_400_80);
   public static final ColorPalette TERTIARY = new ColorPalette("Tertiary", Colors.WHITE_80, Colors.WHITE, Colors.WHITE_80);
   public static final ColorPalette PRIMARY_DESTRUCTIVE = new ColorPalette("Primary Destructive", Colors.ERROR_700, Colors.ERROR_600, Colors.ERROR_600_80);
   public static final ColorPalette SECONDARY_DESTRUCTIVE = new ColorPalette("Secondary Destructive", Colors.GRAY_500, Colors.ERROR_800, Colors.ERROR_800_80);
   public static final ColorPalette TERTIARY_DESTRUCTIVE = new ColorPalette("Tertiary Destructive", Colors.WHITE_90, Colors.ERROR_600_80, Colors.ERROR_600_80);
   private final String name;
   private final int colorNormal;
   private final int colorHovered;
   private final int colorPressed;
   private final float[] colorNormalf;
   private final float[] colorHoveredf;
   private final float[] colorPressedf;

   public ColorPalette(int colorNormal, int colorHovered, int colorPressed) {
      this("Unnamed", colorNormal, colorHovered, colorPressed);
   }

   public ColorPalette(String name, int colorNormal, int colorHovered, int colorPressed) {
      this.name = name;
      this.colorNormal = colorNormal;
      this.colorHovered = colorHovered;
      this.colorPressed = colorPressed;
      this.colorNormalf = new float[]{
         ColorUtils.getRed(colorNormal) / 255.0F,
         ColorUtils.getGreen(colorNormal) / 255.0F,
         ColorUtils.getBlue(colorNormal) / 255.0F,
         ColorUtils.getAlpha(colorNormal) / 255.0F
      };
      this.colorHoveredf = new float[]{
         ColorUtils.getRed(colorHovered) / 255.0F,
         ColorUtils.getGreen(colorHovered) / 255.0F,
         ColorUtils.getBlue(colorHovered) / 255.0F,
         ColorUtils.getAlpha(colorHovered) / 255.0F
      };
      this.colorPressedf = new float[]{
         ColorUtils.getRed(colorPressed) / 255.0F,
         ColorUtils.getGreen(colorPressed) / 255.0F,
         ColorUtils.getBlue(colorPressed) / 255.0F,
         ColorUtils.getAlpha(colorPressed) / 255.0F
      };
   }

   public ColorPalette(OneColor colorNormal, OneColor colorHovered, OneColor colorPressed) {
      this(colorNormal.getRGB(), colorHovered.getRGB(), colorPressed.getRGB());
   }

   public ColorPalette(Color colorNormal, Color colorHovered, Color colorPressed) {
      this(colorNormal.getRGB(), colorHovered.getRGB(), colorPressed.getRGB());
   }

   public ColorPalette(Color colorNormal, Color colorHovered, Color colorPressed, Color colorDisabled) {
      this(colorNormal.getRGB(), colorHovered.getRGB(), colorPressed.getRGB());
   }

   public ColorPalette(float[] colorNormal, float[] colorHovered, float[] colorPressed, float[] colorDisabled) {
      this.name = "Unnamed";
      this.colorNormalf = colorNormal;
      this.colorHoveredf = colorHovered;
      this.colorPressedf = colorPressed;
      this.colorNormal = ColorUtils.getColor(colorNormal[0], colorNormal[1], colorNormal[2], colorNormal[3]);
      this.colorHovered = ColorUtils.getColor(colorHovered[0], colorHovered[1], colorHovered[2], colorHovered[3]);
      this.colorPressed = ColorUtils.getColor(colorPressed[0], colorPressed[1], colorPressed[2], colorPressed[3]);
   }

   public int getNormalColor() {
      return this.colorNormal;
   }

   public int getHoveredColor() {
      return this.colorHovered;
   }

   public int getPressedColor() {
      return this.colorPressed;
   }

   public float[] getNormalColorf() {
      return this.colorNormalf;
   }

   public float[] getHoveredColorf() {
      return this.colorHoveredf;
   }

   public float[] getPressedColorf() {
      return this.colorPressedf;
   }

   @Override
   public String toString() {
      return "ColorPalette(name=" + this.name + ", normal=" + this.colorNormal + ", hovered=" + this.colorHovered + ", pressed=" + this.colorPressed + ")";
   }
}
