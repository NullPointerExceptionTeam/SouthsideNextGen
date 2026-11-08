package dev.southside.render.shader;

import org.lwjgl.opengl.GL45C;

public class ShaderStorageBuffer implements AutoCloseable {
   private int buffer;

   private boolean dsa() {
      return false;
   }

   public final int getBufferID() {
      if (this.buffer == 0) {
         this.buffer = this.dsa() ? GL45C.glCreateBuffers() : GL45C.glGenBuffers();
      }

      return this.buffer;
   }

   public void bindBase(int index) {
      GL45C.glBindBufferBase(37074, index, this.getBufferID());
   }

   public void bindRange(int index, long offset, long size) {
      GL45C.glBindBufferRange(37074, index, this.getBufferID(), offset, size);
   }

   public void allocate(long size) {
      if (this.dsa()) {
         GL45C.glNamedBufferData(this.getBufferID(), size, 35048);
      } else {
         GL45C.glBindBuffer(37074, this.getBufferID());
         GL45C.nglBufferData(37074, size, 0L, 35048);
      }
   }

   public void upload(long offset, long size, long data) {
      if (this.dsa()) {
         GL45C.nglNamedBufferSubData(this.getBufferID(), offset, size, data);
      } else {
         GL45C.glBindBuffer(37074, this.getBufferID());
         GL45C.nglBufferSubData(37074, offset, size, data);
      }
   }

   @Override
   public void close() {
      if (this.buffer != 0) {
         GL45C.glDeleteBuffers(this.buffer);
      }

      this.buffer = 0;
   }
}
