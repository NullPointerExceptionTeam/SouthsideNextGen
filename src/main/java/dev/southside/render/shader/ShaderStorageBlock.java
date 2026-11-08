package dev.southside.render.shader;

import lombok.Generated;
import org.lwjgl.opengl.GL45C;

public class ShaderStorageBlock {
   private final Shader shader;
   private final int index;

   public ShaderStorageBlock(Shader shader, int index) {
      this.shader = shader;
      this.index = index;
   }

   public void bind(int ssbo_index) {
      GL45C.glShaderStorageBlockBinding(this.shader.getProgram(), this.index, ssbo_index);
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
