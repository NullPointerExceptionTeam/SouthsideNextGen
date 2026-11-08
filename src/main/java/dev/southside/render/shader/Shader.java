package dev.southside.render.shader;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import lombok.Generated;
import org.lwjgl.opengl.GL45C;

public class Shader {
   private static int currentProgram;
   private int last;
   private int program;
   private final Map<Integer, ShaderStorageBlock> shaderStorageBlocks = new Object2ObjectOpenHashMap();
   private final Map<String, UniformBlock> uniformBlocks = new Object2ObjectOpenHashMap();
   private final Map<String, Uniform> uniforms = new Object2ObjectOpenHashMap();

   public Shader(int program) {
      this.program = program;
      GL45C.glBindFragDataLocation(program, 0, "fragColor");
   }

   public void bind() {
      currentProgram = GL45C.glGetInteger(35725);
      this.last = currentProgram;
      if (currentProgram != this.program) {
         GL45C.glUseProgram(this.program);
         currentProgram = this.program;
      }
   }

   public Uniform find(String name) {
      return this.uniforms.computeIfAbsent(name, __ -> new Uniform(this, name));
   }

   public void upload() {
      for (Uniform value : this.uniforms.values()) {
         value.upload();
      }

      for (UniformBlock value : this.uniformBlocks.values()) {
         value.upload();
      }
   }

   public void bindUniformBlock(String name, int ubo_index) {
      UniformBlock block = this.uniformBlocks.computeIfAbsent(name, __ -> new UniformBlock(this, name));
      block.bind(ubo_index);
   }

   public void bindShaderStorageBlock(int index, int ssbo_index) {
      ShaderStorageBlock block = this.shaderStorageBlocks.computeIfAbsent(index, __ -> new ShaderStorageBlock(this, index));
      block.bind(ssbo_index);
   }

   public void unbind() {
      GL45C.glUseProgram(this.last);
      currentProgram = this.last;
   }

   public void close() {
      GL45C.glDeleteProgram(this.program);
      this.program = 0;
   }

   public boolean useUniformBlock() {
      return !this.uniformBlocks.isEmpty();
   }

   @Generated
   public int getProgram() {
      return this.program;
   }
}
