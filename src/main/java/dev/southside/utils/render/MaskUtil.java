package dev.southside.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11C;

public class MaskUtil {
   public static void defineMask() {
      GlStateManager._depthMask(true);
      GlStateManager._clearDepth(1.0);
      GL11C.glClear(256);
      GlStateManager._enableDepthTest();
      GlStateManager._depthFunc(519);
      GlStateManager._colorMask(false, false, false, false);
   }

   public static void finishDefineMask() {
      GlStateManager._depthMask(false);
      GlStateManager._colorMask(true, true, true, true);
   }

   public static void drawOnMask() {
      GL11C.glDepthFunc(514);
   }

   public static void drawOffMask() {
      GL11C.glDepthFunc(517);
   }

   public static void drawMCMask() {
      GL11C.glDepthFunc(515);
   }

   public static void resetMask() {
      GlStateManager._clearDepth(1.0);
      GL11C.glClear(256);
      GlStateManager._depthFunc(515);
      GlStateManager._depthMask(false);
   }
}
