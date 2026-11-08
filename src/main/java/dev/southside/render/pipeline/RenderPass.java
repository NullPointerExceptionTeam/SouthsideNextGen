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
import net.minecraft.class_1041;
import net.minecraft.class_276;
import net.minecraft.class_286;
import net.minecraft.class_293;
import net.minecraft.class_4184;
import net.minecraft.class_293.class_5595;
import net.minecraft.class_293.class_5596;
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
   private class_5595 indexType;
   protected final class_293 vertexFormat;
   protected final class_5596 mode;
   protected Shader shader;
   protected Matrix4f model_mat;
   protected class_276 framebuffer;
   protected Matrix4f proj_mat;
   protected boolean disableMatrix;
   private FloatBuffer modelView;
   private static FloatBuffer proj;
   private static FloatBuffer screenSize;
   private static long screenSizeAddr;

   public void setVertexBuffer(ByteBuffer indexBuffer) {
      this.vertexBuffer = indexBuffer;
   }

   public void setIndexBuffer(ByteBuffer indexBuffer, class_5595 indexType) {
      this.indexBuffer = indexBuffer;
      this.indexType = indexType;
   }

   public RenderPass(class_293 vertexFormat, class_5596 mode) {
      this.vertexFormat = vertexFormat;
      this.mode = mode;
      this.vertexBufferId = GlStateManager._glGenBuffers();
      this.indexBufferId = GlStateManager._glGenBuffers();
      this.vertexArrayId = GlStateManager._glGenVertexArrays();
      int oldIBO = GL45C.glGetInteger(34965);
      GlStateManager._glBindVertexArray(this.vertexArrayId);
      GlStateManager._glBindBuffer(34962, this.vertexBufferId);
      GlStateManager._glBindBuffer(34963, this.indexBufferId);
      vertexFormat.method_22649();
      GlStateManager._glBindBuffer(34962, 0);
      GlStateManager._glBindBuffer(34963, oldIBO);
      GlStateManager._glBindVertexArray(0);
   }

   public void disableMatrix() {
      this.disableMatrix = true;
   }

   public static void uploadMatrix(Matrix4f proj_mat) {
      UniformBuffer ubo = ShaderFactory.findOrCreateUBO("matrix", 136);
      class_1041 window = MinecraftAccessor.mc.method_22683();
      if (proj == null) {
         proj = MemoryUtil.memAllocFloat(64);
      }

      if (screenSize == null) {
         screenSize = MemoryUtil.memAllocFloat(8);
         screenSizeAddr = MemoryUtil.memAddress0(screenSize);
      }

      proj_mat.get(proj);
      MemoryUtil.memPutFloat(screenSizeAddr, MinecraftAccessor.mc.method_1522().field_1482);
      MemoryUtil.memPutFloat(screenSizeAddr + 4L, MinecraftAccessor.mc.method_1522().field_1481);
      ubo.upload(0L, 64L, MemoryUtil.memAddress0(proj));
      ubo.upload(128L, 8L, screenSizeAddr);
   }

   public Shader shader() {
      return this.shader;
   }

   private int getFBO(class_276 framebuffer) {
      return framebuffer.field_1476;
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
               class_4184 camera = MinecraftAccessor.mc.field_1773.method_19418();
               Matrix4f viewMat = new Matrix4f().rotation(camera.method_23767().conjugate(new Quaternionf()));
               viewMat.mul(RenderSystem.getModelViewStack());
               this.shader.find("ModelMat").push(viewMat);
               this.shader.find("ScreenSize").push2f(MinecraftAccessor.mc.method_1522().field_1482, MinecraftAccessor.mc.method_1522().field_1481);
            } else {
               UniformBuffer ubo = ShaderFactory.findOrCreateUBO("matrix", 136);
               ubo.bindBase(0);
               if (this.modelView == null) {
                  this.modelView = MemoryUtil.memAllocFloat(64);
               }

               class_4184 camera = MinecraftAccessor.mc.field_1773.method_19418();
               Matrix4f viewMat = new Matrix4f().rotation(camera.method_23767().conjugate(new Quaternionf()));
               viewMat.mul(RenderSystem.getModelViewStack());
               viewMat.get(this.modelView);
               ubo.upload(64L, 64L, MemoryUtil.memAddress0(this.modelView));
            }
         }

         this.shader.upload();
         GlStateManager._glBindVertexArray(this.vertexArrayId);
         GlStateManager._glBindBuffer(34962, this.vertexBufferId);
         GlStateManager._glBindBuffer(34963, this.indexBufferId);
         class_286.method_43436();
         GlStateManager._drawElements(this.mode.field_27383, count, 5125, (long)offset * this.indexType.field_27375);
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
   public void setFramebuffer(class_276 framebuffer) {
      this.framebuffer = framebuffer;
   }

   @Generated
   public void setProj_mat(Matrix4f proj_mat) {
      this.proj_mat = proj_mat;
   }
}
