package dev.southside.render.shader.stages;

import dev.southside.render.shader.CompiledStage;
import dev.southside.render.shader.ShaderType;

class VertexStage extends CompiledStage {
   public VertexStage(int id, String identifier, String source) {
      super(ShaderType.VERTEX, id, identifier, source);
   }
}
