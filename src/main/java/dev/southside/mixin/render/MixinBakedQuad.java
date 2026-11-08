package dev.southside.mixin.render;

import dev.southside.mixininterfaces.IBakedQuad;
import net.minecraft.class_777;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({class_777.class})
public abstract class MixinBakedQuad implements IBakedQuad {
   @Shadow
   @Final
   protected int[] field_4175;

   @Override
   public float southside_Nextgen$getX(int vertexI) {
      return Float.intBitsToFloat(this.field_4175[vertexI * 8]);
   }

   @Override
   public float southside_Nextgen$getY(int vertexI) {
      return Float.intBitsToFloat(this.field_4175[vertexI * 8 + 1]);
   }

   @Override
   public float southside_Nextgen$getZ(int vertexI) {
      return Float.intBitsToFloat(this.field_4175[vertexI * 8 + 2]);
   }
}
