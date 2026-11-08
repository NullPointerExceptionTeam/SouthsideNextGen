package dev.southside.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11C;

public class GL {
   private static final GLState rootState = new GLState();
   private static GLState currentState;

   public static void saveRootState() {
      currentState = null;
      rootState.depthSaved = GL11C.glIsEnabled(2929);
      rootState.blendSaved = GL11C.glIsEnabled(3042);
      rootState.cullSaved = GL11C.glIsEnabled(2884);
      rootState.scissorSaved = GL11C.glIsEnabled(3089);
   }

   public static void restoreRootState() {
      currentState = null;
      setState(2929, rootState.depthSaved);
      setState(3042, rootState.blendSaved);
      setState(2884, rootState.cullSaved);
      setState(3089, rootState.scissorSaved);
   }

   public static void pushState() {
      if (currentState == null) {
         currentState = new GLState();
         rootState.child = currentState;
         currentState.parent = rootState;
      } else {
         currentState.child = new GLState();
         currentState.child.parent = currentState;
         currentState = currentState.child;
      }

      currentState.depthSaved = GL11C.glIsEnabled(2929);
      currentState.blendSaved = GL11C.glIsEnabled(3042);
      currentState.cullSaved = GL11C.glIsEnabled(2884);
      currentState.scissorSaved = GL11C.glIsEnabled(3089);
   }

   public static void popState() {
      if (currentState != null) {
         setState(2929, currentState.depthSaved);
         setState(3042, currentState.blendSaved);
         setState(2884, currentState.cullSaved);
         setState(3089, currentState.scissorSaved);
         currentState = currentState.parent;
         currentState.child = null;
      }
   }

   public static void setState(int state, boolean value) {
      switch (state) {
         case 2884:
            if (value) {
               GlStateManager._enableCull();
            } else {
               GlStateManager._disableCull();
            }
            break;
         case 2929:
            if (value) {
               GlStateManager._enableDepthTest();
            } else {
               GlStateManager._disableDepthTest();
            }
            break;
         case 3042:
            if (value) {
               GlStateManager._enableBlend();
            } else {
               GlStateManager._disableBlend();
            }
            break;
         case 3089:
            if (value) {
               GlStateManager._enableScissorTest();
            } else {
               GlStateManager._disableScissorTest();
            }
            break;
         default:
            if (value) {
               GL11C.glEnable(state);
            } else {
               GL11C.glDisable(state);
            }
      }
   }

   public static void enableDepth() {
      GlStateManager._enableDepthTest();
   }

   public static void disableDepth() {
      GlStateManager._disableDepthTest();
   }

   public static void enableBlend() {
      GlStateManager._enableBlend();
      GlStateManager._blendFunc(770, 771);
   }

   public static void disableBlend() {
      GlStateManager._disableBlend();
   }

   public static void enableCull() {
      GlStateManager._enableCull();
   }

   public static void disableCull() {
      GlStateManager._disableCull();
   }
}
