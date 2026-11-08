package cc.polyfrost.oneconfig.platform;

import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;

public interface GLPlatform {
   void drawRect(UMatrixStack var1, float var2, float var3, float var4, float var5, int var6);

   void enableStencil(Runnable var1);

   default float drawText(String text, float x, float y, int color, boolean shadow) {
      return this.drawText(null, text, x, y, color, shadow);
   }

   float drawText(UMatrixStack var1, String var2, float var3, float var4, int var5, boolean var6);

   int getStringWidth(String var1);
}
