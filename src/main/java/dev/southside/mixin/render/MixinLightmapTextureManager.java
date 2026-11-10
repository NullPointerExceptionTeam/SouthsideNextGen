package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.FullBright;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({LightmapTextureManager.class})
public abstract class MixinLightmapTextureManager {
   @ModifyArgs(
      method = {"update(F)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gl/Uniform;set(F)V",
         ordinal = 6
      )
   )
   private void update(Args args) {
      if (Southside.INSTANCE.getModuleManager().get(FullBright.class).getGamma()) {
         args.set(0, 114514.0F);
      }
   }
}
