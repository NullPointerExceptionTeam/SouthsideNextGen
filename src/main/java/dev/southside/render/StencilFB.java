package dev.southside.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

public class StencilFB extends class_276 {
   private static final StencilFB INSTANCE = new StencilFB();
   private static final List<StencilFB> ACTIVE_INSTANCES = new ArrayList<>();
   private int rboStencilDepth;
   private boolean inUse;

   public StencilFB() {
      super(true);
      this.method_1236(1.0F, 1.0F, 1.0F, 0.0F);
   }

   public static boolean framebufferInUse() {
      return !ACTIVE_INSTANCES.isEmpty();
   }

   public static void use(Runnable drawAction) {
      use(class_310.method_1551().method_1522(), drawAction);
   }

   public static void use(class_276 mainBuffer, Runnable drawAction) {
      RenderSystem.assertOnRenderThreadOrInit();
      StencilFB stencilBuffer = INSTANCE;
      stencilBuffer.method_1234(mainBuffer.field_1482, mainBuffer.field_1481);
      GlStateManager._glBindFramebuffer(36008, mainBuffer.field_1476);
      GlStateManager._glBindFramebuffer(36009, stencilBuffer.field_1476);
      GL30C.glBlitFramebuffer(0, 0, mainBuffer.field_1482, mainBuffer.field_1481, 0, 0, stencilBuffer.field_1482, stencilBuffer.field_1481, 17664, 9728);
      stencilBuffer.method_1235(true);
      drawAction.run();
      stencilBuffer.method_1240();
      GlStateManager._glBindFramebuffer(36008, stencilBuffer.field_1476);
      GlStateManager._glBindFramebuffer(36009, mainBuffer.field_1476);
      GL30C.glBlitFramebuffer(0, 0, stencilBuffer.field_1482, stencilBuffer.field_1481, 0, 0, mainBuffer.field_1482, mainBuffer.field_1481, 17408, 9728);
      stencilBuffer.method_1230();
      mainBuffer.method_1235(false);
   }

   public void method_1231(int width, int height) {
      RenderSystem.assertOnRenderThreadOrInit();
      int maxSize = RenderSystem.maxSupportedTextureSize();
      if (width > 0 && width <= maxSize && height > 0 && height <= maxSize) {
         this.field_1480 = width;
         this.field_1477 = height;
         this.field_1482 = width;
         this.field_1481 = height;
         this.field_1476 = GlStateManager.glGenFramebuffers();
         GlStateManager._glBindFramebuffer(36160, this.field_1476);
         this.field_1475 = GlStateManager._genTexture();
         GlStateManager._bindTexture(this.field_1475);
         GlStateManager._texParameter(3553, 10241, 9729);
         GlStateManager._texParameter(3553, 10240, 9729);
         GlStateManager._texImage2D(3553, 0, 32856, width, height, 0, 6408, 5121, null);
         GL30.glFramebufferTexture2D(36160, 36064, 3553, this.field_1475, 0);
         this.rboStencilDepth = GlStateManager.glGenRenderbuffers();
         GlStateManager._glBindRenderbuffer(36161, this.rboStencilDepth);
         GL30.glRenderbufferStorage(36161, 35056, width, height);
         GL30.glFramebufferRenderbuffer(36160, 33306, 36161, this.rboStencilDepth);
         GlStateManager._glBindRenderbuffer(36161, 0);
         this.method_1239();
         this.method_1230();
         this.method_1242();
      } else {
         throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + maxSize + ")");
      }
   }

   public void method_1238() {
      RenderSystem.assertOnRenderThreadOrInit();
      this.method_1242();
      this.method_1240();
      if (this.field_1476 > -1) {
         GlStateManager._glBindFramebuffer(36160, 0);
         GlStateManager._glDeleteFramebuffers(this.field_1476);
         this.field_1476 = -1;
      }

      if (this.rboStencilDepth > -1) {
         GlStateManager._glDeleteRenderbuffers(this.rboStencilDepth);
         this.rboStencilDepth = -1;
      }

      if (this.field_1475 > -1) {
         TextureUtil.releaseTextureId(this.field_1475);
         this.field_1475 = -1;
      }

      this.field_1482 = -1;
      this.field_1481 = -1;
   }

   public void method_1235(boolean setViewport) {
      super.method_1235(setViewport);
      if (!this.inUse) {
         ACTIVE_INSTANCES.add(this);
         this.inUse = true;
      }
   }

   public void method_1240() {
      super.method_1240();
      if (this.inUse) {
         this.inUse = false;
         ACTIVE_INSTANCES.remove(this);
      }
   }
}
