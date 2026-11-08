package dev.southside.render.shader.stages;

import dev.southside.render.shader.CompiledStage;
import dev.southside.render.shader.ShaderType;

public class FragmentStage extends CompiledStage {
   public FragmentStage(int id, String identifier, String source) {
      super(ShaderType.FRAGMENT, id, identifier, source);
   }
}
