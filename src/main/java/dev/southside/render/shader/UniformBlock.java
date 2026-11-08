package dev.southside.render.shader;

import lombok.Generated;
import org.lwjgl.opengl.GL45C;

public class UniformBlock {
   private final Shader shader;
   private final int index;

   public UniformBlock(Shader shader, String name) {
      this.shader = shader;
      this.index = GL45C.glGetUniformBlockIndex(shader.getProgram(), name);
   }

   public void bind(int ubo_index) {
      GL45C.glUniformBlockBinding(this.shader.getProgram(), this.index, ubo_index);
   }

   public void upload() {
   }

   @Generated
   public Shader getShader() {
      return this.shader;
   }

   @Generated
   public int getIndex() {
      return this.index;
   }
}
