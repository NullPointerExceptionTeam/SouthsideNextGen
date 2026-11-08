package cc.polyfrost.oneconfig.renderer.scissor;

import cc.polyfrost.oneconfig.renderer.LwjglManager;

public interface ScissorHelper {
   ScissorHelper INSTANCE = LwjglManager.INSTANCE.getScissorHelper();

   Scissor scissor(long var1, float var3, float var4, float var5, float var6);

   void resetScissor(long var1, Scissor var3);

   void clearScissors(long var1);

   void save();

   void restore(long var1);
}
