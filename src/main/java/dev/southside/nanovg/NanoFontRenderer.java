package dev.southside.nanovg;

import dev.southside.nanovg.util.IOUtil;
import dev.southside.nanovg.util.ResourceUtil;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryUtil;

public class NanoFontRenderer {
   private final String name;
   private int font;
   private float size;

   public String getName() {
      return this.name;
   }

   public void setSize(float size) {
      this.size = size;
   }

   public NanoFontRenderer(String name, String fileName) {
      this.name = name;

      try {
         try (InputStream inputStream = ResourceUtil.getResourceAsStream(fileName + ".ttf")) {
            byte[] data = IOUtil.toByteArray(inputStream);
            ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
            buffer.put(data).flip();
            this.font = NanoVG.nvgCreateFontMem(NanoLoader.ctx, name, buffer, false);
            this.size = 16.0F;
         }
      } catch (IOException var8) {
         throw new RuntimeException(var8);
      }
   }

   public float getHeight(float size) {
      NanoVG.nvgFontFaceId(NanoLoader.ctx, this.font);
      NanoVG.nvgFontSize(NanoLoader.ctx, size);
      float[] bounds = new float[4];
      NanoVG.nvgTextBounds(NanoLoader.ctx, 0.0F, 0.0F, "AaBbCcDdEeFfGgJjYy", bounds);
      return (bounds[3] - bounds[1]) / 2.0F;
   }

   public float getHeight() {
      NanoVG.nvgFontFaceId(NanoLoader.ctx, this.font);
      NanoVG.nvgFontSize(NanoLoader.ctx, this.size);
      float[] bounds = new float[4];
      NanoVG.nvgTextBounds(NanoLoader.ctx, 0.0F, 0.0F, "AaBbCcDdEeFfGgJjYy", bounds);
      return (bounds[3] - bounds[1]) / 2.0F;
   }

   public float getHeight(String text, float size) {
      if (text == null) {
         return 0.0F;
      } else if (text.isEmpty()) {
         return this.getHeight(size);
      } else {
         NanoVG.nvgFontFaceId(NanoLoader.ctx, this.font);
         NanoVG.nvgFontSize(NanoLoader.ctx, size);
         float[] bounds = new float[4];
         NanoVG.nvgTextBounds(NanoLoader.ctx, 0.0F, 0.0F, text, bounds);
         return (bounds[3] - bounds[1]) / 2.0F;
      }
   }

   public float calculateChunkHeight(String text, float maxWidth, int maxRows, float gap, float size) {
      float currentY = 0.0F;

      for (int row = 1; row <= maxRows; row++) {
         String toRender = this.trimStringToWidth(text, maxWidth, size, false, row == maxRows);
         text = text.replace(toRender.replace("...", ""), "");
         currentY += toRender.isEmpty() ? 0.0F : this.getHeight(toRender, size) + gap;
      }

      return currentY;
   }

   public float drawTrimString(String text, float x, float y, float maxWidth, int maxRows, float gap, float size, int align, Color color) {
      float currentY = y;

      for (int row = 1; row <= maxRows; row++) {
         String toRender = this.trimStringToWidth(text, maxWidth, size, false, row == maxRows);
         text = text.replace(toRender.replace("...", ""), "");
         this.renderPlainString(toRender, x, currentY, size, align, color);
         currentY += toRender.isEmpty() ? 0.0F : this.getHeight(toRender, size) + gap;
      }

      return currentY - y;
   }

   public void drawString(String text, float x, float y, float size, int align, Color color) {
      this.renderPlainString(text, x, y, size, align, color);
   }

   public void drawString(String text, float x, float y, int align, Color color) {
      this.renderPlainString(text, x, y, this.size, align, color);
   }

   public void drawString(String text, float x, float y, int align, Color color, boolean shadow) {
      this.drawString(text, x, y, this.size, align, color, shadow);
   }

   public void drawString(String text, float x, float y, float size, int align, Color color, boolean shadow) {
      if (shadow) {
         this.renderPlainString(text, x + 0.5F, y + 0.5F, size, align, new Color(20, 20, 20, color.getAlpha()));
      }

      this.renderPlainString(text, x, y, size, align, color);
   }

   public void setSpacing(float spacing) {
      NanoVG.nvgTextLetterSpacing(NanoLoader.ctx, spacing);
   }

   public void drawTrimBlurString(String text, float x, float y, float maxWidth, int maxRows, float gap, float size, float radius, int align, Color color) {
      float currentY = y;

      for (int row = 1; row <= maxRows; row++) {
         String toRender = this.trimStringToWidth(text, maxWidth + 2.0F, size, false, row == maxRows);
         text = text.replace(toRender.replace("...", ""), "");
         this.renderBlurString(toRender, x, currentY, size, radius, align, color);
         currentY += toRender.isEmpty() ? 0.0F : this.getHeight(toRender, size) + gap;
      }
   }

   public void drawBlurString(String text, float x, float y, float size, float radius, int align, Color color, boolean shadow) {
      if (shadow) {
         this.renderBlurString(text, x + 0.5F, y + 0.5F, size, radius, align, new Color(20, 20, 20, color.getAlpha()));
      }

      this.renderBlurString(text, x, y, size, radius, align, color);
   }

   public void drawString(String text, float x, float y, float size, Color color, boolean shadow) {
      int rgb = color.getRGB();
      if (shadow) {
         rgb = (rgb & 16579836) >> 2 | rgb & 0xFF000000;
      }

      if (shadow) {
         this.renderPlainString(text, x + 0.5F, y + 0.5F, size, 9, new Color(rgb));
      }

      this.renderPlainString(text, x, y, size, 9, color);
   }

   public void drawCenteredString(String text, float x, float y, float size, Color color) {
      this.drawString(text, x - this.getStringWidth(text, size) / 2.0F, y - this.getHeight(size), size, color, true);
   }

   public void drawCenteredString(String text, float x, float y, Color color) {
      this.drawString(text, x - this.getStringWidth(text, this.size) / 2.0F, y - this.getHeight(this.size), this.size, color, true);
   }

   public void drawString(String text, float x, float y, float size, Color color) {
      this.renderPlainString(text, x, y, size, 9, color);
   }

   public void drawString(String text, float x, float y, Color color) {
      this.renderPlainString(text, x, y, this.size, 9, color);
   }

   public void drawString(String text, float x, float y, Color col, boolean shadow) {
      this.drawString(text, x, y, this.size, 9, col, shadow);
   }

   public void drawGlowString(String text, float x, float y, Color color) {
      this.renderGlowString(text, x, y, this.size, 3.0F, 9, color, color);
   }

   public void drawGlowString(String text, float x, float y, Color color, boolean shadow) {
      int rgb = color.getRGB();
      if (shadow) {
         rgb = (rgb & 16579836) >> 2 | rgb & 0xFF000000;
      }

      this.renderPlainString(text, x + 0.5F, y + 0.5F, this.size, 9, new Color(rgb));
      this.drawGlowString(text, x, y, color);
   }

   public void drawGlowString(String text, float x, float y, float size, Color color) {
      this.renderGlowString(text, x, y, size, 3.0F, 9, color, color);
   }

   public void drawGlowString(String text, float x, float y, float size, int align, Color color) {
      this.renderGlowString(text, x, y, size, 3.0F, align, color, color);
   }

   public void drawGlowString(String text, float x, float y, float size, float radius, int align, Color color) {
      this.renderGlowString(text, x, y, size, radius, align, color, color);
   }

   public void drawGlowString(String text, float x, float y, float size, int align, Color textColor, Color glowColor) {
      this.renderGlowString(text, x, y, size, 3.0F, align, textColor, glowColor);
   }

   public void drawGlowString(String text, float x, float y, float size, Color textColor, Color glowColor) {
      this.renderGlowString(text, x, y, size, 3.0F, 9, textColor, glowColor);
   }

   public void drawGradientString(String text, float x, float y, float size, int align, Color left, Color right) {
      this.renderGradientString(text, x, y, x, y, x + this.getStringWidth(text, size), y + this.getHeight(text, size), size, align, left, right);
   }

   public void drawGradientString(
      String text,
      float x,
      float y,
      float gradientStartX,
      float gradientStartY,
      float gradientEndX,
      float gradientEndY,
      float size,
      int align,
      Color left,
      Color right
   ) {
      this.renderGradientString(text, x, y, gradientStartX, gradientStartY, gradientEndX, gradientEndY, size, align, left, right);
   }

   private void renderPlainString(String text, float x, float y, float size, int align, Color color) {
      NanoVG.nvgTextAlign(NanoLoader.ctx, align);
      NanoUtil.fillColor(color);
      this.renderString(text, x, y + 1.0F, size);
   }

   private void renderGradientString(
      String text,
      float x,
      float y,
      float gradientStartX,
      float gradientStartY,
      float gradientEndX,
      float gradientEndY,
      float size,
      int align,
      Color left,
      Color right
   ) {
      NanoVG.nvgTextAlign(NanoLoader.ctx, align);
      float textWidth = this.getStringWidth(text, size);
      gradientEndX = x + textWidth;
      gradientStartY = y - size / 2.0F;
      NVGPaint gradient = NanoUtil.createGradient(x, gradientStartY, gradientEndX, gradientStartY, Color.YELLOW, Color.WHITE);
      NanoVG.nvgFillPaint(NanoLoader.ctx, gradient);
      NanoVG.nvgRect(NanoLoader.ctx, x, y, textWidth, size);
      this.renderString(text, x, y, size);
      NanoVG.nvgFill(NanoLoader.ctx);
      gradient.free();
   }

   private void renderBlurString(String text, float x, float y, float size, float radius, int align, Color color) {
      NanoVG.nvgTextAlign(NanoLoader.ctx, align);
      NanoUtil.fillColor(color);
      NanoVG.nvgFontBlur(NanoLoader.ctx, radius);
      this.renderString(text, x, y + 1.0F, size);
   }

   private void renderGlowString(String text, float x, float y, float size, float radius, int align, Color textColor, Color glowColor) {
      NanoVG.nvgTextAlign(NanoLoader.ctx, align);
      NanoUtil.fillColor(glowColor);
      NanoVG.nvgFontBlur(NanoLoader.ctx, radius);
      this.renderString(text, x, y + 1.0F, size);
      NanoUtil.fillColor(textColor);
      NanoVG.nvgFontBlur(NanoLoader.ctx, 0.0F);
      this.renderString(text, x, y + 1.0F, size);
   }

   private void renderString(String text, float x, float y, float size) {
      NanoVG.nvgFontFaceId(NanoLoader.ctx, this.font);
      NanoVG.nvgFontSize(NanoLoader.ctx, size / 2.0F);
      NanoVG.nvgText(NanoLoader.ctx, x, y, text);
   }

   public float getStringWidth(String text) {
      return this.getStringWidth(text, this.size);
   }

   public float getStringWidth(String text, float size) {
      if (text == null) {
         return 0.0F;
      } else {
         NanoVG.nvgFontFaceId(NanoLoader.ctx, this.font);
         NanoVG.nvgFontSize(NanoLoader.ctx, size);
         float[] bounds = new float[4];
         NanoVG.nvgTextBounds(NanoLoader.ctx, 0.0F, 0.0F, text, bounds);
         return (bounds[2] - bounds[0]) / 2.0F;
      }
   }

   public String trimStringToWidth(String text, float width, float size) {
      return this.trimStringToWidth(text, width, size, false, false);
   }

   public String trimStringToWidth(String text, float width) {
      return this.trimStringToWidth(text, width, this.size, false, false);
   }

   public String trimStringToWidth(String text, float width, boolean reverse) {
      return this.trimStringToWidth(text, width, this.size, reverse, false);
   }

   public String trimStringToWidth(String text, float width, float size, boolean reverse, boolean more) {
      if (text == null) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder();
         int startIndex = reverse ? text.length() - 1 : 0;
         int step = reverse ? -1 : 1;
         String nextChar = "";

         for (int i = startIndex; i <= text.length() - 1 && i >= 0 && this.getStringWidth(builder + nextChar, size) <= width; i += step) {
            builder.append(text.charAt(i));
            nextChar = reverse ? (i == 0 ? "" : String.valueOf(text.charAt(i + step))) : (i == text.length() - 1 ? "" : String.valueOf(text.charAt(i + step)));
         }

         if (reverse) {
            builder.reverse();
         }

         String result = builder.toString();
         if (more && !text.equals(result)) {
            result = reverse ? "..." + result : result + "...";
         }

         return result;
      }
   }
}
