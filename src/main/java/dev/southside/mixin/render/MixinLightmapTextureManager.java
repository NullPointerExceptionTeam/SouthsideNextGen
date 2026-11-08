package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.FullBright;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({class_765.class})
public abstract class MixinLightmapTextureManager {
   @ModifyArgs(
      method = {"method_3313(F)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_278;method_1251(F)V",
         ordinal = 6
      )
   )
   private void update(Args args) {
      if (Southside.INSTANCE.getModuleManager().get(FullBright.class).getGamma()) {
         args.set(0, 114514.0F);
      }
   }
}
