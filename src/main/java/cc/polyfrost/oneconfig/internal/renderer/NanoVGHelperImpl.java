package cc.polyfrost.oneconfig.internal.renderer;

import cc.polyfrost.oneconfig.ColorCodes;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.asset.Image;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.utils.IOUtils;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.NetworkUtils;
import cc.polyfrost.oneconfig.utils.color.ColorUtil;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import dev.southside.event.Stage;
import dev.southside.event.events.FramebufferDrawEvent;
import dev.southside.nanovg.NanoLoader;
import dev.southside.nanovg.gl.state.States;
import dev.southside.orbit.EventHandler;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.function.LongConsumer;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11C;

public final class NanoVGHelperImpl implements NanoVGHelper {
   private long vg = NanoLoader.ctx;
   private static int[] readingPixels = null;
   private static int[] readColors = new int[]{0};
   private boolean drawing = false;
   private boolean goingToCancel = false;
   private float lastAlpha = 1.0F;

   public NanoVGHelperImpl() {
      EventManager.INSTANCE.register(new Object() {
         @EventHandler
         private void onFramebufferRender(FramebufferDrawEvent event) {
            if (event.stage == Stage.POST && NanoVGHelperImpl.this.drawing) {
               if (NanoVGHelperImpl.this.goingToCancel) {
                  NanoVGHelperImpl.this.drawing = false;
                  NanoVGHelperImpl.this.goingToCancel = false;
               } else {
                  NanoVGHelperImpl.this.goingToCancel = true;
               }
            }
         }
      });
   }

   @Override
   public void drawOutline(long vg, float x, float y, float width, float height, float lineWidth, int color) {
      this.drawRect1(vg, x, y, x + width, y + lineWidth, color);
      this.drawRect1(vg, x, y, x + lineWidth, y + height, color);
      this.drawRect1(vg, x, y + height - lineWidth, x + width, y + height, color);
      this.drawRect1(vg, x + width - lineWidth, y, x + width, y + height, color);
   }

   @Override
   public void drawOutline(long vg, double x, double y, double width, double height, double lineWidth, int color) {
      this.drawRect1(vg, x, y, x + width, y + lineWidth, color);
      this.drawRect1(vg, x, y, x + lineWidth, y + height, color);
      this.drawRect1(vg, x, y + height - lineWidth, x + width, y + height, color);
      this.drawRect1(vg, x + width - lineWidth, y, x + width, y + height, color);
   }

   @Override
   public void setupAndDraw(LongConsumer consumer) {
      this.setupAndDraw(1, false, consumer);
   }

   @Override
   public void setupAndDraw(boolean mcScaling, LongConsumer consumer) {
      this.setupAndDraw(1, mcScaling, consumer);
   }

   @Override
   public void setupAndDraw(float scale, LongConsumer consumer) {
      this.setupAndDraw(1, scale, consumer);
   }

   @Override
   public void setupAndDraw(int nvgFlags, boolean mcScaling, LongConsumer consumer) {
      this.drawing = true;
      if (this.vg == -1L) {
         this.vg = NanoVGGL3.nvgCreate(nvgFlags);
         if (this.vg == -1L) {
            throw new RuntimeException("Failed to create nvg context");
         }
      }

      try {
         Platform.getGLPlatform().enableStencil(() -> {
            States.INSTANCE.push();
            NanoVG.nvgSave(NanoLoader.ctx);
            if (mcScaling) {
               NanoVG.nvgBeginFrame(this.vg, UResolution.getScaledWidth(), UResolution.getScaledHeight(), UResolution.getScaleFactor());
            } else {
               NanoVG.nvgBeginFrame(this.vg, UResolution.getWindowWidth(), UResolution.getWindowHeight(), 1.0F);
            }

            consumer.accept(this.vg);
            NanoVG.nvgEndFrame(NanoLoader.ctx);
            NanoVG.nvgRestore(NanoLoader.ctx);
            States.INSTANCE.pop();
         });
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      if (readingPixels != null) {
         int amount = readingPixels[2] * readingPixels[3];
         readColors = new int[amount];
         ByteBuffer buf = BufferUtils.createByteBuffer(readingPixels[2] * readingPixels[3] * 4);
         GL11C.glReadPixels(readingPixels[0], readingPixels[1], readingPixels[2], readingPixels[3], 6408, 5121, buf);

         for (int i = 0; i < amount; i++) {
            readColors[i] = ColorUtils.getColor(buf.get(), buf.get(), buf.get(), buf.get());
         }

         readingPixels = null;
      }
   }

   @Override
   public void setupAndDraw(int nvgFlags, float scale, LongConsumer consumer) {
      this.drawing = true;
      if (this.vg == -1L) {
         this.vg = NanoVGGL3.nvgCreate(nvgFlags);
         if (this.vg == -1L) {
            throw new RuntimeException("Failed to create nvg context");
         }
      }

      try {
         Platform.getGLPlatform().enableStencil(() -> {
            States.INSTANCE.push();
            NanoVG.nvgSave(NanoLoader.ctx);
            NanoVG.nvgBeginFrame(this.vg, UResolution.getWindowWidth() / scale, UResolution.getWindowHeight() / scale, scale);
            consumer.accept(this.vg);
            NanoVG.nvgEndFrame(NanoLoader.ctx);
            NanoVG.nvgRestore(NanoLoader.ctx);
            States.INSTANCE.pop();
         });
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      if (readingPixels != null) {
         int amount = readingPixels[2] * readingPixels[3];
         readColors = new int[amount];
         ByteBuffer buf = BufferUtils.createByteBuffer(readingPixels[2] * readingPixels[3] * 4);
         GL11C.glReadPixels(readingPixels[0], readingPixels[1], readingPixels[2], readingPixels[3], 6408, 5121, buf);

         for (int i = 0; i < amount; i++) {
            readColors[i] = ColorUtils.getColor(buf.get(), buf.get(), buf.get(), buf.get());
         }

         readingPixels = null;
      }
   }

   @Override
   public void drawRect(long vg, float x, float y, float width, float height, int color) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRect(vg, x, y, width, height);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgFill(vg);
      nvgColor.free();
   }

   @Override
   public void drawRect(long vg, double x, double y, double width, double height, int color) {
      this.drawRect(vg, (float)x, (float)y, (float)width, (float)height, color);
   }

   @Override
   public void drawRect1(long vg, double x, double y, double x1, double y1, int color) {
      this.drawRect(vg, (double)((float)x), (double)((float)y), (float)x1 - x, (float)y1 - y, color);
   }

   @Override
   public void drawSmoothRoundRect(long vg, float x, float y, float w, float h, float rTL, float rTR, float rBR, float rBL, int color) {
      NVGColor nvgColor = NanoVGHelper.INSTANCE.color(vg, color);
      float k = 0.625F;
      float r = 1.25F;
      NanoVG.nvgBeginPath(vg);
      float maxR = Math.min(w, h) / (2.0F * r);
      rTL = Math.min(rTL, maxR);
      rTR = Math.min(rTR, maxR);
      rBR = Math.min(rBR, maxR);
      rBL = Math.min(rBL, maxR);
      if (rTL > 0.0F) {
         float s = rTL * r;
         NanoVG.nvgMoveTo(vg, x, y + s);
         NanoVG.nvgBezierTo(vg, x, y + s * (1.0F - k), x + s * (1.0F - k), y, x + s, y);
      } else {
         NanoVG.nvgMoveTo(vg, x, y);
      }

      if (rTR > 0.0F) {
         float s = rTR * r;
         NanoVG.nvgLineTo(vg, x + w - s, y);
         NanoVG.nvgBezierTo(vg, x + w - s * (1.0F - k), y, x + w, y + s * (1.0F - k), x + w, y + s);
      } else {
         NanoVG.nvgLineTo(vg, x + w, y);
      }

      if (rBR > 0.0F) {
         float s = rBR * r;
         NanoVG.nvgLineTo(vg, x + w, y + h - s);
         NanoVG.nvgBezierTo(vg, x + w, y + h - s * (1.0F - k), x + w - s * (1.0F - k), y + h, x + w - s, y + h);
      } else {
         NanoVG.nvgLineTo(vg, x + w, y + h);
      }

      if (rBL > 0.0F) {
         float s = rBL * r;
         NanoVG.nvgLineTo(vg, x + s, y + h);
         NanoVG.nvgBezierTo(vg, x + s * (1.0F - k), y + h, x, y + h - s * (1.0F - k), x, y + h - s);
      } else {
         NanoVG.nvgLineTo(vg, x, y + h);
      }

      NanoVG.nvgClosePath(vg);
      NanoVG.nvgFillColor(vg, nvgColor);
      NanoVG.nvgFill(vg);
   }

   @Override
   public void drawSmoothRoundRect(long vg, float x, float y, float w, float h, int color, float radius) {
      this.drawSmoothRoundRect(vg, x, y, w, h, radius, radius, radius, radius, color);
   }

   @Override
   public void drawRoundedRect(long vg, float x, float y, float width, float height, int color, float radius) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgFill(vg);
      nvgColor.free();
   }

   @Override
   public void drawRoundedRectVaried(
      long vg, float x, float y, float width, float height, int color, float radiusTL, float radiusTR, float radiusBR, float radiusBL
   ) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRectVarying(vg, x, y, width, height, radiusTL, radiusTR, radiusBR, radiusBL);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgFill(vg);
      nvgColor.free();
   }

   @Override
   public void drawHollowRoundRect(long vg, float x, float y, float width, float height, int color, float radius, float thickness) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x + thickness, y + thickness, width - thickness, height - thickness, radius);
      NanoVG.nvgStrokeWidth(vg, thickness + 0.5F);
      NanoVG.nvgPathWinding(vg, 2);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgStrokeColor(vg, nvgColor);
      NanoVG.nvgStroke(vg);
      nvgColor.free();
   }

   @Override
   public void drawGradientRect(long vg, float x, float y, float width, float height, int color, int color2, NanoVGHelper.GradientDirection direction) {
      NVGPaint bg = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRect(vg, x, y, width, height);
      NVGColor nvgColor = this.color(vg, color);
      NVGColor nvgColor2 = this.color(vg, color2);
      float[] pts = NanoVGHelper.GradientDirection.getValues(x, y, width, height, direction);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, pts[0], pts[1], pts[2], pts[3], nvgColor, nvgColor2, bg));
      NanoVG.nvgFillPaint(vg, bg);
      NanoVG.nvgFill(vg);
      nvgColor.free();
      nvgColor2.free();
   }

   @Override
   public void drawGradientRoundedRect(
      long vg, float x, float y, float width, float height, int color, int color2, float radius, NanoVGHelper.GradientDirection direction
   ) {
      NVGPaint bg = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
      NVGColor nvgColor = this.color(vg, color);
      NVGColor nvgColor2 = this.color(vg, color2);
      float[] pts = NanoVGHelper.GradientDirection.getValues(x, y, width, height, direction);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, pts[0], pts[1], pts[2], pts[3], nvgColor, nvgColor2, bg));
      NanoVG.nvgFill(vg);
      nvgColor.free();
      nvgColor2.free();
   }

   @Override
   public void drawHSBBox(long vg, float x, float y, float width, float height, int colorTarget) {
      this.drawRoundedRect(vg, x, y, width, height, colorTarget, 8.0F);
      NVGPaint bg = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, 8.0F);
      NVGColor nvgColor = this.color(vg, -1);
      NVGColor nvgColor2 = this.color(vg, Colors.TRANSPARENT);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, x, y, x + width, y, nvgColor, nvgColor2, bg));
      NanoVG.nvgFill(vg);
      nvgColor.free();
      nvgColor2.free();
      NVGPaint bg2 = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, 8.0F);
      NVGColor nvgColor3 = this.color(vg, Colors.TRANSPARENT);
      NVGColor nvgColor4 = this.color(vg, Colors.BLACK);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, x, y, x, y + height, nvgColor3, nvgColor4, bg2));
      NanoVG.nvgFill(vg);
      nvgColor3.free();
      nvgColor4.free();
   }

   @Override
   public void drawCircle(long vg, float x, float y, float radius, int color) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgCircle(vg, x, y, radius);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgFill(vg);
      nvgColor.free();
   }

   @Override
   public void drawEllipse(long vg, float x, float y, float radiusX, float radiusY, int color) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgEllipse(vg, x, y, radiusX, radiusY);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgFill(vg);
      nvgColor.free();
   }

   @Override
   public void drawHollowEllipse(long vg, float x, float y, float radiusX, float radiusY, int color, float thickness) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgEllipse(vg, x, y, radiusX, radiusY);
      NanoVG.nvgStrokeWidth(vg, thickness + 0.5F);
      NanoVG.nvgPathWinding(vg, 2);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgStrokeColor(vg, nvgColor);
      NanoVG.nvgStroke(vg);
      nvgColor.free();
   }

   @Override
   public void drawArc(long vg, float cx, float cy, float r, float a0, float a1, int dir, int color, float thickness) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgArc(vg, cx, cy, r, a0, a1, dir);
      NanoVG.nvgStrokeWidth(vg, thickness + 0.5F);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgStrokeColor(vg, nvgColor);
      NanoVG.nvgStroke(vg);
      nvgColor.free();
   }

   @Override
   public void drawTextWithFormatting(long vg, String text, float x, float y, int color, float size, Font font) {
      float offset = 0.0F;
      int currentColor = color;

      for (int i = 0; i < text.length(); i++) {
         String s = String.valueOf(text.charAt(i));
         if (s.equals("§") && i + 1 < text.length()) {
            int colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
            if (colorIndex != -1) {
               switch (colorIndex) {
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                  case 10:
                  case 11:
                  case 12:
                  case 13:
                  case 14:
                  case 15:
                     currentColor = ColorUtil.overwriteAlpha(ColorCodes.colorCode[colorIndex], 255);
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                  case 20:
                  default:
                     break;
                  case 21:
                     currentColor = color;
               }

               i++;
               continue;
            }
         }

         this.drawText(vg, s, x + offset, y, currentColor, size, font);
         offset += this.getTextWidth(vg, s, size, font);
      }
   }

   @Override
   public void drawRawTextWithFormatting(long vg, String text, float x, float y, int color, float size, Font font) {
      float offset = 0.0F;
      int currentColor = color;

      for (int i = 0; i < text.length(); i++) {
         String s = String.valueOf(text.charAt(i));
         if (s.equals("§") && i + 1 < text.length()) {
            int colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
            if (colorIndex != -1) {
               switch (colorIndex) {
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                  case 10:
                  case 11:
                  case 12:
                  case 13:
                  case 14:
                  case 15:
                     currentColor = ColorUtil.overwriteAlpha(ColorCodes.colorCode[colorIndex], 255);
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                  case 20:
                  default:
                     break;
                  case 21:
                     currentColor = color;
               }

               i++;
               continue;
            }
         }

         this.drawRawText(vg, s, x + offset, y, currentColor, size, font);
         offset += this.getTextWidth(vg, s, size, font);
      }
   }

   @Override
   public void drawRawText(long vg, String text, float x, float y, int color, float size, Font font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, size);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 9);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgText(vg, x, y, text);
      nvgColor.free();
   }

   @Override
   public void drawText(long vg, String text, float x, float y, int color, float size, Font font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, size);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 17);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgText(vg, x, y, text);
      nvgColor.free();
   }

   @Override
   public void drawCenteredText(long vg, String text, float x, float y, int color, float size, Font font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, size);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 18);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgText(vg, x, y, text);
      nvgColor.free();
   }

   @Override
   public void drawWrappedString(long vg, String text, float x, float y, float width, int color, float size, Font font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, size);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 17);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgTextBox(vg, x, y, width, text);
      nvgColor.free();
   }

   @Override
   public void drawWrappedString(long vg, String text, float x, float y, float width, int color, float size, float lineHeight, Font font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, size);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, lineHeight);
      NanoVG.nvgTextAlign(vg, 17);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgTextBox(vg, x, y, width, text);
      nvgColor.free();
   }

   @Override
   public float[] getWrappedStringBounds(long vg, String text, float width, float fontSize, Font font) {
      float[] bounds = new float[4];
      NanoVG.nvgFontSize(vg, fontSize);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, width, text, bounds);
      return bounds;
   }

   @Override
   public float[] getWrappedStringBounds(long vg, String text, float width, float fontSize, float lineHeight, Font font) {
      float[] bounds = new float[4];
      NanoVG.nvgFontSize(vg, fontSize);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, lineHeight);
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, width, text, bounds);
      return bounds;
   }

   @Override
   public float getWrappedStringHeight(long vg, String text, float width, float fontSize, float lineHeight, Font font) {
      float[] bounds = new float[4];
      NanoVG.nvgFontSize(vg, fontSize);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, lineHeight);
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, width, text, bounds);
      return bounds[3] - bounds[1];
   }

   @Override
   public float getWrappedStringWidth(long vg, String text, float width, float fontSize, Font font) {
      float[] bounds = new float[4];
      NanoVG.nvgFontSize(vg, fontSize);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, width, text, bounds);
      return bounds[2] - bounds[0];
   }

   @Override
   public float getWrappedStringWidth(long vg, String text, float width, float fontSize, float lineHeight, Font font) {
      float[] bounds = new float[4];
      NanoVG.nvgFontSize(vg, fontSize);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, lineHeight);
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, width, text, bounds);
      return bounds[2] - bounds[0];
   }

   @Override
   public void drawURL(long vg, String url, float x, float y, float size, Font font, InputHandler inputHandler) {
      this.drawText(vg, url, x, y, Colors.PRIMARY_500, size, font);
      float length = this.getTextWidth(vg, url, size, font);
      this.drawRect(vg, x, y + size / 2.0F, length, 1.0F, Colors.PRIMARY_500);
      if (inputHandler.isAreaClicked((int)(x - 2.0F), (int)(y - 1.0F), (int)(length + 4.0F), (int)(size / 2.0F + 3.0F))) {
         NetworkUtils.browseLink(url);
      }
   }

   @Deprecated
   @Override
   public void drawImage(long vg, String filePath, float x, float y, float width, float height) {
      this.drawImage(vg, filePath, x, y, width, height, IOUtils.class);
   }

   @Override
   public void drawImage(long vg, String filePath, float x, float y, float width, float height, Class<?> clazz) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.loadImage(vg, filePath, clazz)) {
         NVGPaint imagePaint = NVGPaint.calloc();
         int image = assetHelper.getImage(filePath);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0F, image, 1.0F, imagePaint);
         NanoVG.nvgRect(vg, x, y, width, height);
         NanoVG.nvgFillPaint(vg, imagePaint);
         NanoVG.nvgFill(vg);
         imagePaint.free();
      }
   }

   @Deprecated
   @Override
   public void drawImage(long vg, String filePath, float x, float y, float width, float height, int color) {
      this.drawImage(vg, filePath, x, y, width, height, color, IOUtils.class);
   }

   @Override
   public void drawImage(long vg, String filePath, float x, float y, float width, float height, int color, Class<?> clazz) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.loadImage(vg, filePath, clazz)) {
         NVGPaint imagePaint = NVGPaint.calloc();
         int image = assetHelper.getImage(filePath);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0F, image, 1.0F, imagePaint);
         this.drawImageCommon(vg, x, y, width, height, color, imagePaint);
         imagePaint.free();
      }
   }

   private void drawImageCommon(long vg, float x, float y, float width, float height, int color, NVGPaint imagePaint) {
      NanoVG.nvgRGBA((byte)(color >> 16 & 0xFF), (byte)(color >> 8 & 0xFF), (byte)(color & 0xFF), (byte)(color >> 24 & 0xFF), imagePaint.innerColor());
      NanoVG.nvgRect(vg, x, y, width, height);
      NanoVG.nvgFillPaint(vg, imagePaint);
      NanoVG.nvgFill(vg);
   }

   @Override
   public void drawImage(long vg, Image image, float x, float y, float width, float height) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.loadImage(vg, image)) {
         this.drawImage(vg, image.filePath, x, y, width, height, image.getClass());
      }
   }

   @Override
   public void drawImage(long vg, Image image, float x, float y, float width, float height, int color) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.loadImage(vg, image)) {
         this.drawImage(vg, image.filePath, x, y, width, height, color, image.getClass());
      }
   }

   @Deprecated
   @Override
   public void drawRoundImage(long vg, String filePath, float x, float y, float width, float height, float radius) {
      this.drawRoundImage(vg, filePath, x, y, width, height, radius, IOUtils.class);
   }

   @Override
   public void drawRoundImage(long vg, String filePath, float x, float y, float width, float height, float radius, Class<?> clazz) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.loadImage(vg, filePath, clazz)) {
         NVGPaint imagePaint = NVGPaint.calloc();
         int image = assetHelper.getImage(filePath);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0F, image, 1.0F, imagePaint);
         NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
         NanoVG.nvgFillPaint(vg, imagePaint);
         NanoVG.nvgFill(vg);
         imagePaint.free();
      }
   }

   @Deprecated
   @Override
   public void drawRoundImage(long vg, Image image, float x, float y, float width, float height, float radius) {
      if (AssetHelper.INSTANCE.loadImage(vg, image)) {
         this.drawRoundImage(vg, image.filePath, x, y, width, height, radius, image.getClass());
      }
   }

   @Override
   public float getTextWidth(long vg, String text, float fontSize, Font font) {
      for (int i = 0; i < text.length(); i++) {
         String s = String.valueOf(text.charAt(i));
         if (s.equals("§") && i + 1 < text.length()) {
            text = text.replace(s + text.charAt(i + 1), "");
         }
      }

      float[] bounds = new float[4];
      NanoVG.nvgFontSize(vg, fontSize);
      NanoVG.nvgFontFace(vg, font.getName());
      return NanoVG.nvgTextBounds(vg, 0.0F, 0.0F, text, bounds);
   }

   @Override
   public float getTextHeight(long vg, float fontSize, Font font) {
      NanoVG.nvgFontSize(vg, fontSize);
      float[] ascender = new float[1];
      float[] descender = new float[1];
      float[] lineh = new float[1];
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextMetrics(vg, ascender, descender, lineh);
      return lineh[0];
   }

   @Override
   public void drawLine(long vg, float x, float y, float endX, float endY, float width, int color) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgMoveTo(vg, x, y);
      NanoVG.nvgLineTo(vg, endX, endY);
      NVGColor nvgColor = this.color(vg, color);
      NanoVG.nvgStrokeColor(vg, nvgColor);
      NanoVG.nvgStrokeWidth(vg, width);
      NanoVG.nvgStroke(vg);
      nvgColor.free();
   }

   @Override
   public void drawDropShadow(long vg, float x, float y, float w, float h, float blur, float spread, float cornerRadius) {
      this.drawDropShadow(vg, x, y, w, h, blur, spread, cornerRadius, new Color(0, 0, 0, 127));
   }

   @Override
   public void drawDropShadow(long vg, float x, float y, float w, float h, float blur, float spread, float cornerRadius, Color color) {
      NVGPaint shadowPaint = NVGPaint.calloc();

      try {
         NVGColor firstColor = NVGColor.calloc();

         try {
            NVGColor secondColor = NVGColor.calloc();

            try {
               this.fillNVGColorWithRGBA(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F, firstColor);
               this.fillNVGColorWithRGBA(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 0.0F, secondColor);
               NanoVG.nvgBoxGradient(
                  vg, x - spread, y - spread, w + 2.0F * spread, h + 2.0F * spread, cornerRadius + spread, blur, firstColor, secondColor, shadowPaint
               );
               NanoVG.nvgBeginPath(vg);
               NanoVG.nvgRoundedRect(
                  vg, x - spread - blur, y - spread - blur, w + 2.0F * spread + 2.0F * blur, h + 2.0F * spread + 2.0F * blur, cornerRadius + spread
               );
               NanoVG.nvgRoundedRect(vg, x, y, w, h, cornerRadius);
               NanoVG.nvgPathWinding(vg, 2);
               NanoVG.nvgFillPaint(vg, shadowPaint);
               NanoVG.nvgFill(vg);
            } catch (Throwable var19) {
               if (secondColor != null) {
                  try {
                     secondColor.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (secondColor != null) {
               secondColor.close();
            }
         } catch (Throwable var20) {
            if (firstColor != null) {
               try {
                  firstColor.close();
               } catch (Throwable var17) {
                  var20.addSuppressed(var17);
               }
            }

            throw var20;
         }

         if (firstColor != null) {
            firstColor.close();
         }
      } catch (Throwable var21) {
         if (shadowPaint != null) {
            try {
               shadowPaint.close();
            } catch (Throwable var16) {
               var21.addSuppressed(var16);
            }
         }

         throw var21;
      }

      if (shadowPaint != null) {
         shadowPaint.close();
      }
   }

   @Override
   public void fillNVGColorWithRGBA(float r, float g, float b, float a, NVGColor color) {
      color.r(r);
      color.g(g);
      color.b(b);
      color.a(a);
   }

   @Override
   public NVGColor color(long vg, int color) {
      NVGColor nvgColor = NVGColor.calloc();
      NanoVG.nvgRGBA((byte)(color >> 16 & 0xFF), (byte)(color >> 8 & 0xFF), (byte)(color & 0xFF), (byte)(color >> 24 & 0xFF), nvgColor);
      NanoVG.nvgFillColor(vg, nvgColor);
      return nvgColor;
   }

   @Override
   public void scale(long vg, float x, float y) {
      NanoVG.nvgScale(vg, x, y);
   }

   @Override
   public void translate(long vg, float x, float y) {
      NanoVG.nvgTranslate(vg, x, y);
   }

   @Override
   public void rotate(long vg, double angle) {
      NanoVG.nvgRotate(vg, (float)Math.toRadians(angle));
   }

   @Override
   public void save(long vg) {
      NanoVG.nvgSave(vg);
   }

   @Override
   public void restore(long vg) {
      NanoVG.nvgRestore(vg);
   }

   @Override
   public void resetTransform(long vg) {
      NanoVG.nvgResetTransform(vg);
   }

   @Override
   public void setAlpha(long vg, float alpha) {
      if (OneConfigGui.INSTANCE != null && OneConfigGui.INSTANCE.isDrawing() && alpha > OneConfigGui.INSTANCE.transparencyFactor) {
         alpha = OneConfigGui.INSTANCE.transparencyFactor;
      }

      this.lastAlpha = alpha;
      NanoVG.nvgGlobalAlpha(vg, alpha);
   }

   @Override
   public float lastAlpha() {
      return this.lastAlpha;
   }

   @Deprecated
   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height, float scale) {
      this.drawSvg(vg, filePath, x, y, width, height, scale, IOUtils.class);
   }

   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height, float scale, Class<?> clazz) {
      float w = width * scale;
      float h = height * scale;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.loadSVG(vg, filePath, w, h, clazz)) {
         NVGPaint imagePaint = NVGPaint.calloc();
         int image = assetHelper.getSVG(filePath, w, h);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0F, image, 1.0F, imagePaint);
         NanoVG.nvgRect(vg, x, y, width, height);
         NanoVG.nvgFillPaint(vg, imagePaint);
         NanoVG.nvgFill(vg);
         imagePaint.free();
      }
   }

   @Deprecated
   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height) {
      this.drawSvg(vg, filePath, x, y, width, height, IOUtils.class);
   }

   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height, Class<?> clazz) {
      if (OneConfigGui.isOpen()) {
         this.drawSvg(vg, filePath, x, y, width, height, OneConfigGui.getScaleFactor(), clazz);
      } else {
         this.drawSvg(vg, filePath, x, y, width, height, 1.0F, clazz);
      }
   }

   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height, int color, float scale) {
      this.drawSvg(vg, filePath, x, y, width, height, color, scale, IOUtils.class);
   }

   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height, int color, float scale, Class<?> clazz) {
      float w = width * scale;
      float h = height * scale;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.loadSVG(vg, filePath, w, h, clazz)) {
         NVGPaint imagePaint = NVGPaint.calloc();
         int image = assetHelper.getSVG(filePath, w, h);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0F, image, 1.0F, imagePaint);
         this.drawImageCommon(vg, x, y, width, height, color, imagePaint);
         imagePaint.free();
      }
   }

   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height, int color) {
      this.drawSvg(vg, filePath, x, y, width, height, color, IOUtils.class);
   }

   @Override
   public void drawSvg(long vg, String filePath, float x, float y, float width, float height, int color, Class<?> clazz) {
      if (OneConfigGui.isOpen()) {
         this.drawSvg(vg, filePath, x, y, width, height, color, OneConfigGui.getScaleFactor(), clazz);
      } else {
         this.drawSvg(vg, filePath, x, y, width, height, color, 1.0F, clazz);
      }
   }

   @Override
   public void drawSvg(long vg, SVG svg, float x, float y, float width, float height, float scale) {
      this.drawSvg(vg, svg.filePath, x, y, width, height, scale, svg.getClass());
   }

   @Override
   public void drawSvg(long vg, SVG svg, float x, float y, float width, float height) {
      this.drawSvg(vg, svg.filePath, x, y, width, height, svg.getClass());
   }

   @Override
   public void drawSvg(long vg, SVG svg, float x, float y, float width, float height, int color, float scale) {
      this.drawSvg(vg, svg.filePath, x, y, width, height, color, scale, svg.getClass());
   }

   @Override
   public void drawSvg(long vg, SVG svg, float x, float y, float width, float height, int color) {
      if (OneConfigGui.isOpen()) {
         this.drawSvg(vg, svg, x, y, width, height, color, OneConfigGui.getScaleFactor());
      } else {
         this.drawSvg(vg, svg, x, y, width, height, color, 1.0F);
      }
   }

   @Override
   public void drawInfo(long vg, InfoType type, float x, float y, float size) {
      SVG icon = null;
      int colorOuter = 0;
      int colorInner = 0;
      switch (type) {
         case INFO:
            icon = SVGs.INFO_CIRCLE;
            colorOuter = Colors.GRAY_400;
            colorInner = Colors.GRAY_300;
            break;
         case SUCCESS:
            icon = SVGs.CHECK_CIRCLE;
            colorOuter = Colors.SUCCESS_700;
            colorInner = Colors.SUCCESS_600;
            break;
         case WARNING:
            icon = SVGs.WARNING;
            colorOuter = Colors.WARNING_600;
            colorInner = Colors.WARNING_500;
            break;
         case ERROR:
            icon = SVGs.ERROR;
            colorOuter = Colors.ERROR_700;
            colorInner = Colors.ERROR_600;
      }

      float centerX = x + size / 2.0F;
      float centerY = y + size / 2.0F;
      this.drawCircle(vg, centerX, centerY, size / 2.0F, colorOuter);
      this.drawCircle(vg, centerX, centerY, size / 2.0F - size / 12.0F, colorInner);
      float iconSize = size / 1.75F;
      this.drawSvg(vg, icon, centerX - iconSize / 2.0F, centerY - iconSize / 2.0F, iconSize, iconSize);
   }

   @Override
   public int[] readPixels(int x, int y, int width, int height) {
      readingPixels = new int[]{x, y, width, height};
      int[] colors = readColors;
      readColors = new int[]{0};
      return colors;
   }

   @Override
   public boolean isDrawing() {
      return this.drawing;
   }

   @Deprecated
   @Override
   public int drawBorderedText(String text, float x, float y, int color, int opacity) {
      return TextRenderer.drawBorderedText(text, x, y, color, opacity);
   }

   @Deprecated
   @Override
   public void drawScaledString(String text, float x, float y, int color, NanoVGHelper.TextType type, float scale) {
      TextRenderer.drawScaledString(text, x, y, color, TextRenderer.TextType.toType(type.ordinal()), scale);
   }
}
