package dev.southside.nanovg;

import cc.polyfrost.oneconfig.renderer.font.FontHelper;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;

public class NanoLoader {
   public static long ctx;

   public static void init() {
      ctx = NanoVGGL3.nvgCreate(3);
      NanoVG.nvgShapeAntiAlias(ctx, true);
      GL.createCapabilities();
      FontHelper.INSTANCE.initialize(ctx);
   }

   public static void destroy() {
      NanoVGGL3.nvgDelete(ctx);
   }
}
