package dev.southside.render.shader;

import lombok.Generated;
import org.lwjgl.opengl.GL45C;

public class CompiledStage {
   private final ShaderType type;
   private int id;
   private final String identifier;
   private final String source;

   public boolean isVertexShader() {
      return this.type == ShaderType.VERTEX;
   }

   public boolean isFragmentShader() {
      return this.type == ShaderType.FRAGMENT;
   }

   public void attach(int program) {
      GL45C.glAttachShader(program, this.id);
   }

   public void detach(int program) {
      GL45C.glDetachShader(program, this.id);
   }

   public void close() {
      if (this.id != 0) {
         GL45C.glDeleteShader(this.id);
         this.id = 0;
      }
   }

   @Generated
   public ShaderType getType() {
      return this.type;
   }

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   public String getIdentifier() {
      return this.identifier;
   }

   @Generated
   public String getSource() {
      return this.source;
   }

   @Generated
   public CompiledStage(ShaderType type, int id, String identifier, String source) {
      this.type = type;
      this.id = id;
      this.identifier = identifier;
      this.source = source;
   }
}
