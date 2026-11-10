package dev.southside.mixin.render;

import dev.southside.mixininterfaces.IBakedQuad;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({BakedQuad.class})
public abstract class MixinBakedQuad implements IBakedQuad {
   @Shadow
   @Final
   protected int[] vertexData;

   @Override
   public float southside_Nextgen$getX(int vertexI) {
      return Float.intBitsToFloat(this.vertexData[vertexI * 8]);
   }

   @Override
   public float southside_Nextgen$getY(int vertexI) {
      return Float.intBitsToFloat(this.vertexData[vertexI * 8 + 1]);
   }

   @Override
   public float southside_Nextgen$getZ(int vertexI) {
      return Float.intBitsToFloat(this.vertexData[vertexI * 8 + 2]);
   }
}
