package dev.southside.render.shader;

import lombok.Generated;

public enum ShaderType {
   VERTEX("Vertex", 35633),
   FRAGMENT("Fragment", 35632);

   public final String name;
   public final int nativeType;

   @Generated
   private ShaderType(final String name, final int nativeType) {
      this.name = name;
      this.nativeType = nativeType;
   }
}
