package cc.polyfrost.oneconfig.config.core;

import java.awt.Color;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

public final class OneColor implements Serializable, Cloneable, Comparable<OneColor> {
   private transient Integer argb = null;
   public short[] hsba;
   public int dataBit = -1;

   public OneColor(String hex) {
      this.hsba = new short[]{0, 0, 0, 0};
      if (hex.length() > 7) {
         this.hsba[3] = (short)Integer.parseInt(hex.substring(6, 8), 16);
      }

      this.setColorFromHex(hex);
   }

   public OneColor(int argb) {
      this.argb = argb;
      this.hsba = ARGBtoHSBA(this.argb);
   }

   public OneColor(int r, int g, int b, int a) {
      this.argb = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
      this.hsba = ARGBtoHSBA(this.argb);
   }

   public OneColor(int r, int g, int b) {
      this(r, g, b, 255);
   }

   public OneColor(@NotNull Color c) {
      this(c.getRGB());
   }

   public OneColor(float hue, float saturation, float brightness, float alpha) {
      this.hsba = new short[]{(short)hue, (short)saturation, (short)brightness, (short)alpha};
      this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
   }

   public OneColor(float hue, float saturation, float brightness) {
      this(hue, saturation, brightness, 1.0F);
   }

   public OneColor(int saturation, int brightness, int alpha, float chromaSpeed) {
      this(
         (float)(System.currentTimeMillis() % (int)(chromaSpeed * 1000.0F)) / (chromaSpeed * 1000.0F) * 360.0F,
         (float)saturation,
         (float)brightness,
         (float)alpha
      );
      if (chromaSpeed < 1.0F) {
         chromaSpeed = 1.0F;
      }

      if (chromaSpeed > 30.0F) {
         chromaSpeed = 30.0F;
      }

      this.dataBit = (int)chromaSpeed * 1000;
   }

   public OneColor(int hue, int saturation, int brightness, int alpha, int chromaSpeed) {
      if (chromaSpeed == -1) {
         this.hsba = new short[]{(short)hue, (short)saturation, (short)brightness, (short)alpha};
         this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
      } else {
         this.dataBit = chromaSpeed;
         this.hsba = new short[]{(short)hue, (short)saturation, (short)brightness, (short)alpha};
      }
   }

   public static int HSBAtoARGB(float hue, float saturation, float brightness, int alpha) {
      int temp = Color.HSBtoRGB(hue / 360.0F, saturation / 100.0F, brightness / 100.0F);
      return temp & 16777215 | alpha << 24;
   }

   public static short[] ARGBtoHSBA(int rgba) {
      short[] hsb = new short[4];
      float[] hsbArray = Color.RGBtoHSB(rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF, null);
      hsb[0] = (short)(hsbArray[0] * 360.0F);
      hsb[1] = (short)(hsbArray[1] * 100.0F);
      hsb[2] = (short)(hsbArray[2] * 100.0F);
      hsb[3] = (short)(rgba >> 24 & 0xFF);
      return hsb;
   }

   public int getRed() {
      return this.getRGB() >> 16 & 0xFF;
   }

   public int getGreen() {
      return this.getRGB() >> 8 & 0xFF;
   }

   public int getBlue() {
      return this.getRGB() & 0xFF;
   }

   public int getHue() {
      return this.hsba[0];
   }

   public int getSaturation() {
      return this.hsba[1];
   }

   public int getBrightness() {
      return this.hsba[2];
   }

   public int getAlpha() {
      return this.hsba[3];
   }

   public void setAlpha(int alpha) {
      this.hsba[3] = (short)alpha;
      this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
   }

   public int getDataBit() {
      return this.dataBit == -1 ? -1 : this.dataBit / 1000;
   }

   public void setChromaSpeed(int speed) {
      if (speed == -1) {
         this.dataBit = -1;
      } else {
         if (speed < 1) {
            speed = 1;
         }

         if (speed > 30) {
            speed = 30;
         }

         this.dataBit = speed * 1000;
      }
   }

   public void setHSBA(int hue, int saturation, int brightness, int alpha) {
      this.hsba[0] = (short)hue;
      this.hsba[1] = (short)saturation;
      this.hsba[2] = (short)brightness;
      this.hsba[3] = (short)alpha;
      this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
   }

   public void setHSBA(int index, int val) {
      this.hsba[index] = (short)val;
   }

   public short[] getHSBA() {
      return this.hsba;
   }

   public void setFromOneColor(OneColor color) {
      this.setHSBA(color.hsba[0], color.hsba[1], color.hsba[2], color.hsba[3]);
   }

   public int getRGB() {
      if (this.dataBit == 0) {
         this.dataBit = -1;
      }

      if (this.dataBit == -1) {
         if (this.argb == null) {
            this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
         }

         return this.argb;
      } else {
         int temp = Color.HSBtoRGB((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit, this.hsba[1] / 100.0F, this.hsba[2] / 100.0F);
         this.hsba[0] = (short)((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit * 360.0F);
         return temp & 16777215 | this.hsba[3] << 24;
      }
   }

   public int getRGBNoAlpha() {
      return 0xFF000000 | this.getRGB();
   }

   public int getRGBMax(boolean maxBrightness) {
      if (this.dataBit == 0) {
         this.dataBit = -1;
      }

      if (this.dataBit == -1) {
         return HSBAtoARGB(this.hsba[0], 100.0F, maxBrightness ? 100.0F : 0.0F, this.hsba[3]);
      } else {
         int temp = Color.HSBtoRGB((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit, 1.0F, maxBrightness ? 1.0F : 0.0F);
         this.hsba[0] = (short)((float)(System.currentTimeMillis() % this.dataBit) / this.dataBit * 360.0F);
         return temp & 16777215 | this.hsba[3] << 24;
      }
   }

   public String getHex() {
      return Integer.toHexString(0xFF000000 | this.getRGB()).toUpperCase().substring(2);
   }

   public void setColorFromHex(String hex) {
      hex = hex.replace("#", "");
      if (hex.length() == 8) {
         this.hsba[3] = (short)Integer.parseInt(hex.substring(6, 8), 16);
      }

      if (hex.length() > 6) {
         hex = hex.substring(0, 6);
      }

      if (hex.length() == 3) {
         hex = this.charsToString(hex.charAt(0), hex.charAt(0), hex.charAt(1), hex.charAt(1), hex.charAt(2), hex.charAt(2));
      }

      if (hex.length() == 1) {
         hex = this.charsToString(hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0));
      }

      if (hex.length() == 2 && hex.charAt(1) == hex.charAt(0)) {
         hex = this.charsToString(hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0));
      }

      StringBuilder hexBuilder = new StringBuilder(hex);

      while (hexBuilder.length() < 6) {
         hexBuilder.append("0");
      }

      hex = hexBuilder.toString();
      int r = Integer.valueOf(hex.substring(0, 2), 16);
      int g = Integer.valueOf(hex.substring(2, 4), 16);
      int b = Integer.valueOf(hex.substring(4, 6), 16);
      this.argb = (this.getAlpha() & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
      this.hsba = ARGBtoHSBA(this.argb);
   }

   private String charsToString(char... chars) {
      StringBuilder sb = new StringBuilder();

      for (char c : chars) {
         sb.append(c);
      }

      return sb.toString();
   }

   public Color toJavaColor() {
      return new Color(this.getRGB(), true);
   }

   @Override
   public String toString() {
      return "OneColor{rgba=[r="
         + this.getRed()
         + ", g="
         + this.getGreen()
         + ", b="
         + this.getBlue()
         + ", a="
         + this.getAlpha()
         + "], hsba=[h="
         + this.getHue()
         + ", s="
         + this.getSaturation()
         + ", b="
         + this.getBrightness()
         + ", a="
         + this.getAlpha()
         + "], hex="
         + this.getHex()
         + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (o == this) {
         return true;
      } else {
         return !(o instanceof OneColor color)
            ? false
            : this.getHue() == color.getHue()
               && this.getSaturation() == color.getSaturation()
               && this.getBrightness() == color.getBrightness()
               && this.getAlpha() == color.getAlpha()
               && this.getDataBit() == color.getDataBit();
      }
   }

   public int compareTo(@NotNull OneColor o) {
      return this.getHue() - o.getHue() + this.getSaturation() - o.getSaturation() + this.getBrightness() - o.getBrightness() + this.getAlpha() - o.getAlpha();
   }

   @NotNull
   public OneColor clone() {
      return new OneColor((float)this.getHue(), (float)this.getSaturation(), (float)this.getBrightness(), (float)this.getAlpha());
   }

   @NotNull
   public OneColor copy() {
      return this.clone();
   }
}
