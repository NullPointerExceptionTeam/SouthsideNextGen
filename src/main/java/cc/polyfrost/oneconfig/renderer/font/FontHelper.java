package cc.polyfrost.oneconfig.renderer.font;

import cc.polyfrost.oneconfig.renderer.LwjglManager;

public interface FontHelper {
   FontHelper INSTANCE = LwjglManager.INSTANCE.getFontHelper();

   void initialize(long var1);

   void loadFont(long var1, Font var3);
}
