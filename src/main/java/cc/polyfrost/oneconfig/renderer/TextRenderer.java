package cc.polyfrost.oneconfig.renderer;

import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.platform.Platform;
import java.util.regex.Pattern;

public class TextRenderer {
   private static final Pattern regex = Pattern.compile("(?i)§[0-9a-f]");
   private static boolean drawingBorder = false;

   public static int drawBorderedText(String text, float x, float y, int color, int opacity) {
      String noColors = regex.matcher(text).replaceAll("§r");
      drawingBorder = true;
      int yes = 0;
      if (opacity / 4 > 3) {
         for (int xOff = -2; xOff <= 2; xOff++) {
            for (int yOff = -2; yOff <= 2; yOff++) {
               if (xOff * xOff != yOff * yOff) {
                  yes = (int)(yes + Platform.getGLPlatform().drawText(noColors, xOff / 2.0F + x, yOff / 2.0F + y, opacity / 4 << 24, false));
               }
            }
         }
      }

      yes += (int)Platform.getGLPlatform().drawText(text, x, y, color, false);
      drawingBorder = false;
      return yes;
   }

   public static float getStringWidth(String text) {
      return Platform.getGLPlatform().getStringWidth(text);
   }

   public static void drawScaledString(String text, float x, float y, int color, TextRenderer.TextType type, float scale) {
      UGraphics.GL.pushMatrix();
      UGraphics.GL.scale(scale, scale, 1.0F);
      switch (type) {
         case NONE:
            Platform.getGLPlatform().drawText(text, x * (1.0F / scale), y * (1.0F / scale), color, false);
            break;
         case SHADOW:
            Platform.getGLPlatform().drawText(text, x * (1.0F / scale), y * (1.0F / scale), color, true);
            break;
         case FULL:
            drawBorderedText(text, x * (1.0F / scale), y * (1.0F / scale), color, 255);
      }

      UGraphics.GL.popMatrix();
   }

   public static boolean isDrawingTextBorder() {
      return drawingBorder;
   }

   public static enum TextType {
      NONE,
      SHADOW,
      FULL;

      public static TextRenderer.TextType toType(int type) {
         return values()[type];
      }
   }
}
