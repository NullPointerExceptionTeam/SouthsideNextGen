package cc.polyfrost.oneconfig.renderer;

@Deprecated
public class RenderManager {
   public static int drawBorderedText(String text, float x, float y, int color, int opacity) {
      return TextRenderer.drawBorderedText(text, x, y, color, opacity);
   }

   public static void drawScaledString(String text, float x, float y, int color, RenderManager.TextType type, float scale) {
      TextRenderer.drawScaledString(text, x, y, color, TextRenderer.TextType.toType(type.ordinal()), scale);
   }

   public static enum TextType {
      NONE,
      SHADOW,
      FULL;

      public static RenderManager.TextType toType(int type) {
         return values()[type];
      }
   }
}
