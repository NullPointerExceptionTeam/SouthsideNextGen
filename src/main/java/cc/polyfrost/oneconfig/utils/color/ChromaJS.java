package cc.polyfrost.oneconfig.utils.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_3532;

public class ChromaJS {
   public static class Scale {
      public final Color firstColor;
      public final Color lastColor;
      public final int numColors;
      public final List<Color> colors;
      private static final double Kn = 18.0;
      private static final double Xn = 0.95047;
      private static final double Yn = 1.0;
      private static final double Zn = 1.08883;
      private static final double t0 = 0.137931034;
      private static final double t1 = 0.206896552;
      private static final double t2 = 0.12841855;
      private static final double t3 = 0.008856452;

      public Scale(Color firstColor, Color lastColor, int numColors) {
         this.firstColor = firstColor;
         this.lastColor = lastColor;
         this.numColors = numColors;
         this.colors = new ArrayList<>();
         this.initialize();
      }

      public void initialize() {
         int dm = 0;
         int dd = 1;

         for (int i = 0; i < this.numColors; i++) {
            this.colors.add(this.getColor(dm + (double)i / (this.numColors - 1) * dd));
         }
      }

      private Color getColor(double val) {
         int _min = 0;
         int _max = 1;
         double t = (val - 0.0) / 1.0;
         t = Math.min(1.0, Math.max(0.0, t));
         int k = (int)Math.floor(t * 10000.0);
         Color col = null;

         for (int p = 0; p <= 1; p++) {
            if (t <= p) {
               col = p == 0 ? this.firstColor : this.lastColor;
               break;
            }

            if (t >= p && p == 1) {
               col = p == 0 ? this.firstColor : this.lastColor;
               break;
            }

            if (t > p && t < p + 1) {
               t = (t - p) / (p + 1 - p);
               col = this.interpolate(this.firstColor, this.lastColor, (float)t);
               break;
            }
         }

         return col;
      }

      public static float[] convertToHCL(Color color) {
         float[] lab = new float[3];
         RGBtoLab(color.getRed(), color.getGreen(), color.getBlue(), lab);
         float[] hcl = new float[3];
         LabtoHCL(lab[0], lab[1], lab[2], hcl);
         return hcl;
      }

      private static void RGBtoLab(int r, int g, int b, float[] lab) {
         float[] xyz = new float[3];
         float[] scaledRGB = new float[]{r / 255.0F, g / 255.0F, b / 255.0F};

         for (int i = 0; i < 3; i++) {
            scaledRGB[i] = scaledRGB[i] > 0.04045F ? (float)Math.pow((scaledRGB[i] + 0.055) / 1.055, 2.4) : scaledRGB[i] / 12.92F;
            scaledRGB[i] = (float)(scaledRGB[i] * 100.0);
         }

         xyz[0] = scaledRGB[0] * 0.4124564F + scaledRGB[1] * 0.3575761F + scaledRGB[2] * 0.1804375F;
         xyz[1] = scaledRGB[0] * 0.2126729F + scaledRGB[1] * 0.7151522F + scaledRGB[2] * 0.072175F;
         xyz[2] = scaledRGB[0] * 0.0193339F + scaledRGB[1] * 0.119192F + scaledRGB[2] * 0.9503041F;
         xyz[0] /= 95.047F;
         xyz[1] /= 100.0F;
         xyz[2] /= 108.883F;

         for (int i = 0; i < 3; i++) {
            xyz[i] = xyz[i] > 0.008856F ? (float)Math.pow(xyz[i], 0.3333333333333333) : 7.787F * xyz[i] + 0.13793103F;
         }

         lab[0] = 116.0F * xyz[1] - 16.0F;
         lab[1] = 500.0F * (xyz[0] - xyz[1]);
         lab[2] = 200.0F * (xyz[1] - xyz[2]);
      }

      private static void LabtoHCL(float L, float a, float b, float[] hcl) {
         float C = (float)Math.sqrt(a * a + b * b);
         float H = (float)Math.toDegrees(Math.atan2(b, a));
         if (H < 0.0F) {
            H += 360.0F;
         }

         hcl[0] = H;
         hcl[1] = C;
         hcl[2] = L;
      }

      public int[] lch2rgb(double l, double c, double h) {
         double[] lab = this.lch2lab(l, c, h);
         return this.lab2rgb(lab[0], lab[1], lab[2]);
      }

      private double[] lch2lab(double l, double c, double h) {
         if (Double.isNaN(h)) {
            h = 0.0;
         }

         h = Math.toRadians(h);
         return new double[]{l, Math.cos(h) * c, Math.sin(h) * c};
      }

      private int[] lab2rgb(double l, double a, double b) {
         double y = (l + 16.0) / 116.0;
         double x = Double.isNaN(a) ? y : y + a / 500.0;
         double z = Double.isNaN(b) ? y : y - b / 200.0;
         y = 1.0 * this.lab_xyz(y);
         x = 0.95047 * this.lab_xyz(x);
         z = 1.08883 * this.lab_xyz(z);
         double r = this.xyz_rgb(3.2404542 * x - 1.5371385 * y - 0.4985314 * z);
         double g = this.xyz_rgb(-0.969266 * x + 1.8760108 * y + 0.041556 * z);
         double b_ = this.xyz_rgb(0.0556434 * x - 0.2040259 * y + 1.0572252 * z);
         return new int[]{(int)Math.round(r), (int)Math.round(g), (int)Math.round(b_), 255};
      }

      private double xyz_rgb(double r) {
         return 255.0 * (r <= 0.00304 ? 12.92 * r : Math.pow(r, 0.4166666666666667) * 1.055 - 0.055);
      }

      private double lab_xyz(double t) {
         return t > 0.206896552 ? t * t * t : 0.12841855 * (t - 0.137931034);
      }

      private Color hclToColor(float[] hcl) {
         int[] rgb = this.lch2rgb(hcl[2], hcl[1], hcl[0]);

         for (int i = 0; i < 3; i++) {
            rgb[i] = class_3532.method_15340(rgb[i], 0, 255);
         }

         return new Color(rgb[0], rgb[1], rgb[2]);
      }

      private Color interpolate(Color col1, Color col2, float f) {
         float[] xyz0 = convertToHCL(col1);
         float[] xyz1 = convertToHCL(col2);
         float hue0 = xyz0[0];
         float hue1 = xyz1[0];
         float sat0 = xyz0[1];
         float sat1 = xyz1[1];
         float lbv0 = xyz0[2];
         float lbv1 = xyz1[2];
         float sat = -1.0F;
         float hue;
         if (!Float.isNaN(hue0) && !Float.isNaN(hue1)) {
            float dh;
            if (hue1 > hue0 && hue1 - hue0 > 180.0F) {
               dh = hue1 - (hue0 + 360.0F);
            } else if (hue1 < hue0 && hue0 - hue1 > 180.0F) {
               dh = hue1 + 360.0F - hue0;
            } else {
               dh = hue1 - hue0;
            }

            hue = hue0 + f * dh;
         } else if (!Float.isNaN(hue0)) {
            hue = hue0;
            if (lbv1 == 1.0F || lbv1 == 0.0F) {
               sat = sat0;
            }
         } else if (!Float.isNaN(hue1)) {
            hue = hue1;
            if (lbv0 == 1.0F || lbv0 == 0.0F) {
               sat = sat1;
            }
         } else {
            hue = Float.NaN;
         }

         if (sat == -1.0F) {
            sat = sat0 + f * (sat1 - sat0);
         }

         float lbv = lbv0 + f * (lbv1 - lbv0);
         return this.hclToColor(new float[]{hue, sat, lbv});
      }

      public int getColorRGB(double rate) {
         rate = class_3532.method_15350(rate, 0.0, 1.0);
         int count = (int)Math.floor(Math.max(0.0, rate * (this.numColors - 1) - 0.001));
         return ColorUtil.interpolateColor(
            this.colors.get(count), this.colors.get(count + 1), (float)((rate - (double)count / (this.numColors - 1)) * (this.numColors - 1))
         );
      }
   }
}
