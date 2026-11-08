package cc.polyfrost.oneconfig.renderer;

import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.renderer.asset.Image;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.utils.InputHandler;
import java.awt.Color;
import java.util.function.LongConsumer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.nanovg.NVGColor;

public interface NanoVGHelper {
   NanoVGHelper INSTANCE = LwjglManager.INSTANCE.getNanoVGHelper();

   void drawOutline(long var1, float var3, float var4, float var5, float var6, float var7, int var8);

   void drawOutline(long var1, double var3, double var5, double var7, double var9, double var11, int var13);

   void setupAndDraw(LongConsumer var1);

   void setupAndDraw(boolean var1, LongConsumer var2);

   void setupAndDraw(float var1, LongConsumer var2);

   void setupAndDraw(int var1, boolean var2, LongConsumer var3);

   void setupAndDraw(int var1, float var2, LongConsumer var3);

   void drawRect(long var1, float var3, float var4, float var5, float var6, int var7);

   void drawRect(long var1, double var3, double var5, double var7, double var9, int var11);

   void drawRect1(long var1, double var3, double var5, double var7, double var9, int var11);

   void drawSmoothRoundRect(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, int var11);

   void drawSmoothRoundRect(long var1, float var3, float var4, float var5, float var6, int var7, float var8);

   void drawRoundedRect(long var1, float var3, float var4, float var5, float var6, int var7, float var8);

   void drawRoundedRectVaried(long var1, float var3, float var4, float var5, float var6, int var7, float var8, float var9, float var10, float var11);

   void drawHollowRoundRect(long var1, float var3, float var4, float var5, float var6, int var7, float var8, float var9);

   void drawGradientRect(long var1, float var3, float var4, float var5, float var6, int var7, int var8, NanoVGHelper.GradientDirection var9);

   void drawGradientRoundedRect(long var1, float var3, float var4, float var5, float var6, int var7, int var8, float var9, NanoVGHelper.GradientDirection var10);

   void drawHSBBox(long var1, float var3, float var4, float var5, float var6, int var7);

   void drawCircle(long var1, float var3, float var4, float var5, int var6);

   void drawEllipse(long var1, float var3, float var4, float var5, float var6, int var7);

   void drawHollowEllipse(long var1, float var3, float var4, float var5, float var6, int var7, float var8);

   void drawArc(long var1, float var3, float var4, float var5, float var6, float var7, int var8, int var9, float var10);

   void drawTextWithFormatting(long var1, String var3, float var4, float var5, int var6, float var7, Font var8);

   void drawRawText(long var1, String var3, float var4, float var5, int var6, float var7, Font var8);

   void drawRawTextWithFormatting(long var1, String var3, float var4, float var5, int var6, float var7, Font var8);

   void drawText(long var1, String var3, float var4, float var5, int var6, float var7, Font var8);

   void drawCenteredText(long var1, String var3, float var4, float var5, int var6, float var7, Font var8);

   void drawWrappedString(long var1, String var3, float var4, float var5, float var6, int var7, float var8, Font var9);

   void drawWrappedString(long var1, String var3, float var4, float var5, float var6, int var7, float var8, float var9, Font var10);

   float[] getWrappedStringBounds(long var1, String var3, float var4, float var5, Font var6);

   float[] getWrappedStringBounds(long var1, String var3, float var4, float var5, float var6, Font var7);

   float getWrappedStringHeight(long var1, String var3, float var4, float var5, float var6, Font var7);

   float getWrappedStringWidth(long var1, String var3, float var4, float var5, Font var6);

   float getWrappedStringWidth(long var1, String var3, float var4, float var5, float var6, Font var7);

   void drawURL(long var1, String var3, float var4, float var5, float var6, Font var7, InputHandler var8);

   @Deprecated
   void drawImage(long var1, String var3, float var4, float var5, float var6, float var7);

   void drawImage(long var1, String var3, float var4, float var5, float var6, float var7, Class<?> var8);

   @Deprecated
   void drawImage(long var1, String var3, float var4, float var5, float var6, float var7, int var8);

   void drawImage(long var1, String var3, float var4, float var5, float var6, float var7, int var8, Class<?> var9);

   void drawImage(long var1, Image var3, float var4, float var5, float var6, float var7);

   void drawImage(long var1, Image var3, float var4, float var5, float var6, float var7, int var8);

   @Deprecated
   void drawRoundImage(long var1, String var3, float var4, float var5, float var6, float var7, float var8);

   void drawRoundImage(long var1, String var3, float var4, float var5, float var6, float var7, float var8, Class<?> var9);

   void drawRoundImage(long var1, Image var3, float var4, float var5, float var6, float var7, float var8);

   float getTextWidth(long var1, String var3, float var4, Font var5);

   float getTextHeight(long var1, float var3, Font var4);

   void drawLine(long var1, float var3, float var4, float var5, float var6, float var7, int var8);

   void drawDropShadow(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9);

   void drawDropShadow(long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, Color var10);

   void fillNVGColorWithRGBA(float var1, float var2, float var3, float var4, NVGColor var5);

   NVGColor color(long var1, int var3);

   void scale(long var1, float var3, float var4);

   void resetTransform(long var1);

   void setAlpha(long var1, float var3);

   float lastAlpha();

   @Deprecated
   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7, float var8);

   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7, float var8, Class<?> var9);

   @Deprecated
   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7);

   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7, Class<?> var8);

   @Deprecated
   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7, int var8, float var9);

   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7, int var8, float var9, Class<?> var10);

   @Deprecated
   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7, int var8);

   void drawSvg(long var1, String var3, float var4, float var5, float var6, float var7, int var8, Class<?> var9);

   void drawSvg(long var1, SVG var3, float var4, float var5, float var6, float var7, float var8);

   void drawSvg(long var1, SVG var3, float var4, float var5, float var6, float var7);

   void drawSvg(long var1, SVG var3, float var4, float var5, float var6, float var7, int var8, float var9);

   void drawSvg(long var1, SVG var3, float var4, float var5, float var6, float var7, int var8);

   void drawInfo(long var1, InfoType var3, float var4, float var5, float var6);

   int[] readPixels(int var1, int var2, int var3, int var4);

   boolean isDrawing();

   @Deprecated
   int drawBorderedText(String var1, float var2, float var3, int var4, int var5);

   @Deprecated
   void drawScaledString(String var1, float var2, float var3, int var4, NanoVGHelper.TextType var5, float var6);

   void translate(long var1, float var3, float var4);

   void rotate(long var1, double var3);

   void save(long var1);

   void restore(long var1);

   public static enum GradientDirection {
      DOWN,
      UP,
      LEFT,
      RIGHT,
      DIAGONAL_DOWN,
      DIAGONAL_UP;

      public static float[] getValues(float x, float y, float width, float height, @NotNull NanoVGHelper.GradientDirection direction) {
         switch (direction) {
            case DOWN:
            default:
               return new float[]{x, y, x, y + height};
            case UP:
               return new float[]{x, y + height, x, y};
            case LEFT:
               return new float[]{x + width, y, x, y};
            case RIGHT:
               return new float[]{x, y, x + width, y};
            case DIAGONAL_DOWN:
               return new float[]{x, y, x + width, y + height};
            case DIAGONAL_UP:
               return new float[]{x, y + height, x + width, y};
         }
      }
   }

   public static enum TextType {
      NONE,
      SHADOW,
      FULL;

      public static NanoVGHelper.TextType toType(int type) {
         return values()[type];
      }
   }
}
