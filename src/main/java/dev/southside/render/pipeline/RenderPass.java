package dev.southside.render.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.MinecraftAccessor;
import dev.southside.render.shader.Shader;
import dev.southside.render.shader.ShaderFactory;
import dev.southside.render.shader.UniformBuffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import lombok.Generated;
import net.minecraft.client.util.Window;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat.IndexType;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

public class RenderPass {
   private final int vertexBufferId;
   private final int indexBufferId;
   private final int vertexArrayId;
   private ByteBuffer vertexBuffer;
   private ByteBuffer indexBuffer;
   private IndexType indexType;
   protected final VertexFormat vertexFormat;
   protected final DrawMode mode;
   protected Shader shader;
   protected Matrix4f model_mat;
   protected Framebuffer framebuffer;
   protected Matrix4f proj_mat;
   protected boolean disableMatrix;
   private FloatBuffer modelView;
   private static FloatBuffer proj;
   private static FloatBuffer screenSize;
   private static long screenSizeAddr;

   public void setVertexBuffer(ByteBuffer indexBuffer) {
      this.vertexBuffer = indexBuffer;
   }

   public void setIndexBuffer(ByteBuffer indexBuffer, IndexType indexType) {
      this.indexBuffer = indexBuffer;
      this.indexType = indexType;
   }

   public RenderPass(VertexFormat vertexFormat, DrawMode mode) {
      this.vertexFormat = vertexFormat;
      this.mode = mode;
      this.vertexBufferId = GlStateManager._glGenBuffers();
      this.indexBufferId = GlStateManager._glGenBuffers();
      this.vertexArrayId = GlStateManager._glGenVertexArrays();
      int oldIBO = GL45C.glGetInteger(34965);
      GlStateManager._glBindVertexArray(this.vertexArrayId);
      GlStateManager._glBindBuffer(34962, this.vertexBufferId);
      GlStateManager._glBindBuffer(34963, this.indexBufferId);
      vertexFormat.setupState();
      GlStateManager._glBindBuffer(34962, 0);
      GlStateManager._glBindBuffer(34963, oldIBO);
      GlStateManager._glBindVertexArray(0);
   }

   public void disableMatrix() {
      this.disableMatrix = true;
   }

   public static void uploadMatrix(Matrix4f proj_mat) {
      UniformBuffer ubo = ShaderFactory.findOrCreateUBO("matrix", 136);
      Window window = MinecraftAccessor.mc.getWindow();
      if (proj == null) {
         proj = MemoryUtil.memAllocFloat(64);
      }

      if (screenSize == null) {
         screenSize = MemoryUtil.memAllocFloat(8);
         screenSizeAddr = MemoryUtil.memAddress0(screenSize);
      }

      proj_mat.get(proj);
      MemoryUtil.memPutFloat(screenSizeAddr, MinecraftAccessor.mc.getFramebuffer().textureWidth);
      MemoryUtil.memPutFloat(screenSizeAddr + 4L, MinecraftAccessor.mc.getFramebuffer().textureHeight);
      ubo.upload(0L, 64L, MemoryUtil.memAddress0(proj));
      ubo.upload(128L, 8L, screenSizeAddr);
   }

   public Shader shader() {
      return this.shader;
   }

   private int getFBO(Framebuffer framebuffer) {
      return framebuffer.fbo;
   }

   public void drawIndexed(int offset, int count) {
      if (this.shader != null) {
         int oldVertexArray = GL45C.glGetInteger(34229);
         int oldIBO = GL45C.glGetInteger(34965);
         this.uploadVertexBuffer(this.vertexBuffer);
         this.uploadIndexBuffer(this.indexBuffer, oldIBO);
         this.shader.bind();
         if (!this.disableMatrix) {
            if (!this.shader.useUniformBlock()) {
               this.shader.find("ProjMat").push(this.proj_mat == null ? RenderSystem.getProjectionMatrix() : this.proj_mat);
               Camera camera = MinecraftAccessor.mc.gameRenderer.getCamera();
               Matrix4f viewMat = new Matrix4f().rotation(camera.getRotation().conjugate(new Quaternionf()));
               viewMat.mul(RenderSystem.getModelViewStack());
               this.shader.find("ModelMat").push(viewMat);
               this.shader.find("ScreenSize").push2f(MinecraftAccessor.mc.getFramebuffer().textureWidth, MinecraftAccessor.mc.getFramebuffer().textureHeight);
            } else {
               UniformBuffer ubo = ShaderFactory.findOrCreateUBO("matrix", 136);
               ubo.bindBase(0);
               if (this.modelView == null) {
                  this.modelView = MemoryUtil.memAllocFloat(64);
               }

               Camera camera = MinecraftAccessor.mc.gameRenderer.getCamera();
               Matrix4f viewMat = new Matrix4f().rotation(camera.getRotation().conjugate(new Quaternionf()));
               viewMat.mul(RenderSystem.getModelViewStack());
               viewMat.get(this.modelView);
               ubo.upload(64L, 64L, MemoryUtil.memAddress0(this.modelView));
            }
         }

         this.shader.upload();
         GlStateManager._glBindVertexArray(this.vertexArrayId);
         GlStateManager._glBindBuffer(34962, this.vertexBufferId);
         GlStateManager._glBindBuffer(34963, this.indexBufferId);
         BufferRenderer.resetCurrentVertexBuffer();
         GlStateManager._drawElements(this.mode.glMode, count, 5125, (long)offset * this.indexType.size);
         GlStateManager._glBindVertexArray(oldVertexArray);
         GlStateManager._glBindBuffer(34963, oldIBO);
         GlStateManager._glBindBuffer(34962, 0);
         this.shader.unbind();
      }
   }

   private void uploadIndexBuffer(ByteBuffer indexBuffer, int old) {
      GlStateManager._glBindBuffer(34963, this.indexBufferId);
      GlStateManager._glBufferData(34963, indexBuffer, 35048);
      GlStateManager._glBindBuffer(34963, old);
   }

   private void uploadVertexBuffer(ByteBuffer buf) {
      GlStateManager._glBindBuffer(34962, this.vertexBufferId);
      GlStateManager._glBufferData(34962, buf, 35048);
      GlStateManager._glBindBuffer(34962, 0);
   }

   @Generated
   public void setShader(Shader shader) {
      this.shader = shader;
   }

   @Generated
   public void setModel_mat(Matrix4f model_mat) {
      this.model_mat = model_mat;
   }

   @Generated
   public void setFramebuffer(Framebuffer framebuffer) {
      this.framebuffer = framebuffer;
   }

   @Generated
   public void setProj_mat(Matrix4f proj_mat) {
      this.proj_mat = proj_mat;
   }
}
