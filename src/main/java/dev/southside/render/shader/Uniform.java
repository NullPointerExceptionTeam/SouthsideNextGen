package dev.southside.render.shader;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import lombok.Generated;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4dc;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

public class Uniform {
   private final Shader shader;
   private final String name;
   private final int location;
   private Runnable upload;
   private final FloatBuffer MATRIX33 = MemoryUtil.memAllocFloat(9);
   private final FloatBuffer MATRIX44 = MemoryUtil.memAllocFloat(16);

   private boolean dsa() {
      return false;
   }

   public Uniform(Shader shader, String name) {
      this.shader = shader;
      this.name = name;
      this.location = GL45C.glGetUniformLocation(shader.getProgram(), name);
   }

   public void upload() {
      if (this.upload != null) {
         this.upload.run();
         this.upload = null;
      }
   }

   public void push(Color c) {
      this.push4f(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
   }

   public void push(Vector2ic c) {
      this.push2i(c.x(), c.y());
   }

   public void push(Vector2fc c) {
      this.push2f(c.x(), c.y());
   }

   public void push(Vector2dc c) {
      this.push2d(c.x(), c.y());
   }

   public void push(Vector3ic c) {
      this.push3i(c.x(), c.y(), c.z());
   }

   public void push(Vector3fc c) {
      this.push3f(c.x(), c.y(), c.z());
   }

   public void push(Vector3dc c) {
      this.push3d(c.x(), c.y(), c.z());
   }

   public void push(Vector4ic c) {
      this.push4i(c.x(), c.y(), c.z(), c.w());
   }

   public void push(Vector4fc c) {
      this.push4f(c.x(), c.y(), c.z(), c.w());
   }

   public void push(Vector4dc c) {
      this.push4d(c.x(), c.y(), c.z(), c.w());
   }

   public void push1i(int value) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform1i(this.shader.getProgram(), this.location, value);
         } else {
            this.shader.bind();
            GL45C.glUniform1i(this.location, value);
         }
      };
   }

   public void push1iv(int[] value) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform1iv(this.shader.getProgram(), this.location, value);
         } else {
            this.shader.bind();
            GL45C.glUniform1iv(this.location, value);
         }
      };
   }

   public void push1f(float value) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform1f(this.shader.getProgram(), this.location, value);
         } else {
            this.shader.bind();
            GL45C.glUniform1f(this.location, value);
         }
      };
   }

   public void push1fv(float[] value) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform1fv(this.shader.getProgram(), this.location, value);
         } else {
            this.shader.bind();
            GL45C.glUniform1fv(this.location, value);
         }
      };
   }

   public void push1d(double value) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform1d(this.shader.getProgram(), this.location, value);
         } else {
            this.shader.bind();
            GL45C.glUniform1d(this.location, value);
         }
      };
   }

   public void push1dv(double[] value) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform1dv(this.shader.getProgram(), this.location, value);
         } else {
            this.shader.bind();
            GL45C.glUniform1dv(this.location, value);
         }
      };
   }

   public void push2f(float x, float y) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform2f(this.shader.getProgram(), this.location, x, y);
         } else {
            this.shader.bind();
            GL45C.glUniform2f(this.location, x, y);
         }
      };
   }

   public void push2fv(float[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform2fv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform2fv(this.location, values);
         }
      };
   }

   public void push3f(float x, float y, float z) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform3f(this.shader.getProgram(), this.location, x, y, z);
         } else {
            this.shader.bind();
            GL45C.glUniform3f(this.location, x, y, z);
         }
      };
   }

   public void push3fv(float[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform3fv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform3fv(this.location, values);
         }
      };
   }

   public void push4f(float x, float y, float z, float w) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform4f(this.shader.getProgram(), this.location, x, y, z, w);
         } else {
            this.shader.bind();
            GL45C.glUniform4f(this.location, x, y, z, w);
         }
      };
   }

   public void push4fv(float[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform4fv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform4fv(this.location, values);
         }
      };
   }

   public void push2i(int x, int y) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform2i(this.shader.getProgram(), this.location, x, y);
         } else {
            this.shader.bind();
            GL45C.glUniform2i(this.location, x, y);
         }
      };
   }

   public void push2iv(int[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform2iv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform2iv(this.location, values);
         }
      };
   }

   public void push3i(int x, int y, int z) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform3i(this.shader.getProgram(), this.location, x, y, z);
         } else {
            this.shader.bind();
            GL45C.glUniform3i(this.location, x, y, z);
         }
      };
   }

   public void push3iv(int[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform3iv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform3iv(this.location, values);
         }
      };
   }

   public void push4i(int x, int y, int z, int w) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform4i(this.shader.getProgram(), this.location, x, y, z, w);
         } else {
            this.shader.bind();
            GL45C.glUniform4i(this.location, x, y, z, w);
         }
      };
   }

   public void push4iv(int[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform4iv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform4iv(this.location, values);
         }
      };
   }

   public void push2d(double x, double y) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform2d(this.shader.getProgram(), this.location, x, y);
         } else {
            this.shader.bind();
            GL45C.glUniform2d(this.location, x, y);
         }
      };
   }

   public void push2dv(double[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform2dv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform2dv(this.location, values);
         }
      };
   }

   public void push3d(double x, double y, double z) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform3d(this.shader.getProgram(), this.location, x, y, z);
         } else {
            this.shader.bind();
            GL45C.glUniform3d(this.location, x, y, z);
         }
      };
   }

   public void push3dv(double[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform3dv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform3dv(this.location, values);
         }
      };
   }

   public void push4d(double x, double y, double z, double w) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform4d(this.shader.getProgram(), this.location, x, y, z, w);
         } else {
            this.shader.bind();
            GL45C.glUniform4d(this.location, x, y, z, w);
         }
      };
   }

   public void push4dv(double[] values) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniform4dv(this.shader.getProgram(), this.location, values);
         } else {
            this.shader.bind();
            GL45C.glUniform4dv(this.location, values);
         }
      };
   }

   public void push(Matrix3fc m) {
      m.get(this.MATRIX33);
      this.pushMatrix3f(this.MATRIX33, false);
   }

   public void push(Matrix4fc m) {
      m.get(this.MATRIX44);
      this.pushMatrix4f(this.MATRIX44, false);
   }

   public void pushTranspose(Matrix3fc m) {
      m.getTransposed(this.MATRIX33);
      this.pushMatrix3f(this.MATRIX33, true);
   }

   public void pushTranspose(Matrix4fc m) {
      m.getTransposed(this.MATRIX44);
      this.pushMatrix4f(this.MATRIX44, true);
   }

   public void pushMatrix2f(float[] matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix2fv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix2fv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix3f(float[] matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix3fv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix3fv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix4f(float[] matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix4fv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix4fv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix2d(double[] matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix2dv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix2dv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix3d(double[] matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix3dv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix3dv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix4d(double[] matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix4dv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix4dv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix2f(FloatBuffer matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix2fv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix2fv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix3f(FloatBuffer matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix3fv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix3fv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix4f(FloatBuffer matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix4fv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix4fv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix2d(DoubleBuffer matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix2dv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix2dv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix3d(DoubleBuffer matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix3dv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix3dv(this.location, transpose, matrix);
         }
      };
   }

   public void pushMatrix4d(DoubleBuffer matrix, boolean transpose) {
      this.upload = () -> {
         if (this.dsa()) {
            GL45C.glProgramUniformMatrix4dv(this.shader.getProgram(), this.location, transpose, matrix);
         } else {
            this.shader.bind();
            GL45C.glUniformMatrix4dv(this.location, transpose, matrix);
         }
      };
   }

   @Generated
   public Shader getShader() {
      return this.shader;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int getLocation() {
      return this.location;
   }

   @Generated
   public Runnable getUpload() {
      return this.upload;
   }

   @Generated
   public FloatBuffer getMATRIX33() {
      return this.MATRIX33;
   }

   @Generated
   public FloatBuffer getMATRIX44() {
      return this.MATRIX44;
   }
}
