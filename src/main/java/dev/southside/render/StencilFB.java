package dev.southside.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

public class StencilFB extends Framebuffer {
   private static final StencilFB INSTANCE = new StencilFB();
   private static final List<StencilFB> ACTIVE_INSTANCES = new ArrayList<>();
   private int rboStencilDepth;
   private boolean inUse;

   public StencilFB() {
      super(true);
      this.setClearColor(1.0F, 1.0F, 1.0F, 0.0F);
   }

   public static boolean framebufferInUse() {
      return !ACTIVE_INSTANCES.isEmpty();
   }

   public static void use(Runnable drawAction) {
      use(MinecraftClient.getInstance().getFramebuffer(), drawAction);
   }

   public static void use(Framebuffer mainBuffer, Runnable drawAction) {
      RenderSystem.assertOnRenderThreadOrInit();
      StencilFB stencilBuffer = INSTANCE;
      stencilBuffer.resize(mainBuffer.textureWidth, mainBuffer.textureHeight);
      GlStateManager._glBindFramebuffer(36008, mainBuffer.fbo);
      GlStateManager._glBindFramebuffer(36009, stencilBuffer.fbo);
      GL30C.glBlitFramebuffer(0, 0, mainBuffer.textureWidth, mainBuffer.textureHeight, 0, 0, stencilBuffer.textureWidth, stencilBuffer.textureHeight, 17664, 9728);
      stencilBuffer.beginWrite(true);
      drawAction.run();
      stencilBuffer.endWrite();
      GlStateManager._glBindFramebuffer(36008, stencilBuffer.fbo);
      GlStateManager._glBindFramebuffer(36009, mainBuffer.fbo);
      GL30C.glBlitFramebuffer(0, 0, stencilBuffer.textureWidth, stencilBuffer.textureHeight, 0, 0, mainBuffer.textureWidth, mainBuffer.textureHeight, 17408, 9728);
      stencilBuffer.clear();
      mainBuffer.beginWrite(false);
   }

   public void initFbo(int width, int height) {
      RenderSystem.assertOnRenderThreadOrInit();
      int maxSize = RenderSystem.maxSupportedTextureSize();
      if (width > 0 && width <= maxSize && height > 0 && height <= maxSize) {
         this.viewportWidth = width;
         this.viewportHeight = height;
         this.textureWidth = width;
         this.textureHeight = height;
         this.fbo = GlStateManager.glGenFramebuffers();
         GlStateManager._glBindFramebuffer(36160, this.fbo);
         this.colorAttachment = GlStateManager._genTexture();
         GlStateManager._bindTexture(this.colorAttachment);
         GlStateManager._texParameter(3553, 10241, 9729);
         GlStateManager._texParameter(3553, 10240, 9729);
         GlStateManager._texImage2D(3553, 0, 32856, width, height, 0, 6408, 5121, null);
         GL30.glFramebufferTexture2D(36160, 36064, 3553, this.colorAttachment, 0);
         this.rboStencilDepth = GlStateManager.glGenRenderbuffers();
         GlStateManager._glBindRenderbuffer(36161, this.rboStencilDepth);
         GL30.glRenderbufferStorage(36161, 35056, width, height);
         GL30.glFramebufferRenderbuffer(36160, 33306, 36161, this.rboStencilDepth);
         GlStateManager._glBindRenderbuffer(36161, 0);
         this.checkFramebufferStatus();
         this.clear();
         this.endRead();
      } else {
         throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + maxSize + ")");
      }
   }

   public void delete() {
      RenderSystem.assertOnRenderThreadOrInit();
      this.endRead();
      this.endWrite();
      if (this.fbo > -1) {
         GlStateManager._glBindFramebuffer(36160, 0);
         GlStateManager._glDeleteFramebuffers(this.fbo);
         this.fbo = -1;
      }

      if (this.rboStencilDepth > -1) {
         GlStateManager._glDeleteRenderbuffers(this.rboStencilDepth);
         this.rboStencilDepth = -1;
      }

      if (this.colorAttachment > -1) {
         TextureUtil.releaseTextureId(this.colorAttachment);
         this.colorAttachment = -1;
      }

      this.textureWidth = -1;
      this.textureHeight = -1;
   }

   public void beginWrite(boolean setViewport) {
      super.beginWrite(setViewport);
      if (!this.inUse) {
         ACTIVE_INSTANCES.add(this);
         this.inUse = true;
      }
   }

   public void endWrite() {
      super.endWrite();
      if (this.inUse) {
         this.inUse = false;
         ACTIVE_INSTANCES.remove(this);
      }
   }
}
