package dev.southside.render.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.MinecraftAccessor;
import dev.southside.render.GL;
import dev.southside.render.shader.Shader;
import java.awt.Color;
import java.nio.ByteBuffer;
import lombok.Generated;
import net.minecraft.class_243;
import net.minecraft.class_293;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5595;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4fStack;
import org.lwjgl.system.MemoryUtil;

public class Mesh {
   public final RenderPass pass;
   private class_4587 matrices;
   private final class_293 format;
   private final class_5596 mode;
   private final int primitiveVerticesSize;
   private final int primitiveIndicesCount;
   private ByteBuffer vertices;
   private long verticesPointerStart;
   private long verticesPointer;
   private ByteBuffer indices;
   private long indicesPointer;
   private int vertexI;
   private int indicesCount;
   private boolean building;
   private boolean in3d;

   public Mesh mat(class_4587 stack) {
      this.matrices = stack;
      return this;
   }

   public Shader shader() {
      return this.pass.shader();
   }

   public Mesh(RenderPass pass) {
      this.pass = pass;
      this.format = pass.vertexFormat;
      this.mode = pass.mode;
      this.indices = MemoryUtil.memAlloc(2048);
      this.indicesPointer = MemoryUtil.memAddress0(this.indices);
      this.vertices = MemoryUtil.memAlloc(1024);
      this.verticesPointer = this.verticesPointerStart = MemoryUtil.memAddress0(this.vertices);
      this.primitiveVerticesSize = this.format.method_1362();
      this.primitiveIndicesCount = this.mode.field_27384;
   }

   public Mesh(Shader shader, class_293 format, class_5596 mode) {
      this.pass = new RenderPass(format, mode);
      this.pass.setShader(shader);
      this.format = format;
      this.mode = mode;
      this.indices = MemoryUtil.memAlloc(2048);
      this.indicesPointer = MemoryUtil.memAddress0(this.indices);
      this.vertices = MemoryUtil.memAlloc(1024);
      this.verticesPointer = this.verticesPointerStart = MemoryUtil.memAddress0(this.vertices);
      this.primitiveVerticesSize = format.method_1362();
      this.primitiveIndicesCount = mode.field_27384;
   }

   public void begin() {
      if (this.building) {
         throw new IllegalStateException("Mesh.begin() called while already building.");
      } else {
         this.building = true;
         this.verticesPointer = this.verticesPointerStart;
         this.vertexI = 0;
         this.indicesCount = 0;
      }
   }

   public Mesh vec3(double x, double y, double z) {
      return this.vec3((float)x, (float)y, (float)z);
   }

   public Mesh vec2(double x, double y) {
      return this.vec3(x, y, 0.0);
   }

   public void enable3d() {
      this.in3d = true;
   }

   public void disable3d() {
      this.in3d = false;
   }

   public Mesh vec3(float x, float y, float z) {
      long p = this.verticesPointer;
      if (this.in3d) {
         float _x = (float)(x - MinecraftAccessor.mc.method_1561().field_4686.method_19326().method_10216());
         float _z = (float)(z - MinecraftAccessor.mc.method_1561().field_4686.method_19326().method_10215());
         MemoryUtil.memPutFloat(p, _x);
         MemoryUtil.memPutFloat(p + 4L, y);
         MemoryUtil.memPutFloat(p + 8L, _z);
      } else {
         MemoryUtil.memPutFloat(p, x);
         MemoryUtil.memPutFloat(p + 4L, y);
         MemoryUtil.memPutFloat(p + 8L, z);
      }

      this.verticesPointer += 12L;
      return this;
   }

   public Mesh vec2(float x, float y) {
      return this.vec3(x, y, 0.0F);
   }

   public Mesh color(float r, float g, float b, float a) {
      return this.color((int)r * 255, (int)g * 255, (int)b * 255, (int)a * 255);
   }

   public Mesh color(int r, int g, int b, int a) {
      long p = this.verticesPointer;
      MemoryUtil.memPutByte(p, (byte)r);
      MemoryUtil.memPutByte(p + 1L, (byte)g);
      MemoryUtil.memPutByte(p + 2L, (byte)b);
      MemoryUtil.memPutByte(p + 3L, (byte)a);
      this.verticesPointer += 4L;
      return this;
   }

   public Mesh color(int c) {
      return this.color(new Color(c));
   }

   public Mesh color(Color c) {
      return this.color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
   }

   public int next() {
      this.ensureCapacity(1, 0);
      return this.vertexI++;
   }

   public void line(int i1, int i2) {
      this.ensureLineCapacity();
      long p = this.indicesPointer + this.indicesCount * 4L;
      MemoryUtil.memPutInt(p, i1);
      MemoryUtil.memPutInt(p + 4L, i2);
      this.indicesCount += 2;
   }

   public void quad(int i1, int i2, int i3, int i4) {
      this.ensureQuadCapacity();
      long p = this.indicesPointer + this.indicesCount * 4L;
      MemoryUtil.memPutInt(p, i1);
      MemoryUtil.memPutInt(p + 4L, i2);
      MemoryUtil.memPutInt(p + 8L, i3);
      MemoryUtil.memPutInt(p + 12L, i3);
      MemoryUtil.memPutInt(p + 16L, i4);
      MemoryUtil.memPutInt(p + 20L, i1);
      this.indicesCount += 6;
   }

   public void triangle(int i1, int i2, int i3) {
      this.ensureTriCapacity();
      long p = this.indicesPointer + this.indicesCount * 4L;
      MemoryUtil.memPutInt(p, i1);
      MemoryUtil.memPutInt(p + 4L, i2);
      MemoryUtil.memPutInt(p + 8L, i3);
      this.indicesCount += 3;
   }

   public void ensureQuadCapacity() {
      this.ensureCapacity(4, 6);
   }

   public void ensureTriCapacity() {
      this.ensureCapacity(3, 3);
   }

   public void ensureLineCapacity() {
      this.ensureCapacity(2, 2);
   }

   public void ensureCapacity(int vertexCount, int indexCount) {
      if ((this.vertexI + vertexCount) * this.primitiveVerticesSize >= this.vertices.capacity()) {
         int offset = this.getVerticesOffset();
         int newSize = this.vertices.capacity() * 2;
         ByteBuffer newVertices = MemoryUtil.memAlloc(newSize);
         MemoryUtil.memCopy(MemoryUtil.memAddress0(this.vertices), MemoryUtil.memAddress0(newVertices), offset);
         this.vertices = newVertices;
         this.verticesPointerStart = MemoryUtil.memAddress0(this.vertices);
         this.verticesPointer = this.verticesPointerStart + offset;
      }

      if ((this.indicesCount + indexCount) * 4 >= this.indices.capacity()) {
         int newSize = this.indices.capacity() * 2;
         ByteBuffer newIndices = MemoryUtil.memAlloc(newSize);
         MemoryUtil.memCopy(MemoryUtil.memAddress0(this.indices), MemoryUtil.memAddress0(newIndices), this.indicesCount * 4L);
         MemoryUtil.memFree(this.indices);
         this.indices = newIndices;
         this.indicesPointer = MemoryUtil.memAddress0(this.indices);
      }
   }

   public void end() {
      if (!this.building) {
         throw new IllegalStateException("Mesh.end() called while not building.");
      } else {
         this.building = false;
         this.pass.setIndexBuffer(this.getIndexBuffer(), class_5595.field_27373);
         this.pass.setVertexBuffer(this.getVertexBuffer());
      }
   }

   public void draw() {
      this.end();
      GL.disableDepth();
      GL.enableBlend();
      GL.disableCull();
      this._draw();
   }

   public void _draw() {
      Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
      matrixStack.pushMatrix();
      if (this.matrices != null) {
         matrixStack.mul(this.matrices.method_23760().method_23761());
      }

      if (this.in3d) {
         class_243 cameraPos = MinecraftAccessor.mc.field_1773.method_19418().method_19326();
         matrixStack.translate(0.0F, (float)(-cameraPos.field_1351), 0.0F);
      }

      this.pass.drawIndexed(0, this.indicesCount);
      matrixStack.popMatrix();
   }

   private int getVerticesOffset() {
      return (int)(this.verticesPointer - this.verticesPointerStart);
   }

   public ByteBuffer getVertexBuffer() {
      this.vertices.limit(this.getVerticesOffset());
      return this.vertices;
   }

   public ByteBuffer getIndexBuffer() {
      this.indices.limit(this.indicesCount * 4);
      return this.indices;
   }

   @Generated
   public int getVertexI() {
      return this.vertexI;
   }

   @Generated
   public int getIndicesCount() {
      return this.indicesCount;
   }

   @Generated
   public boolean isBuilding() {
      return this.building;
   }
}
