package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.class_1297;
import net.minecraft.class_758;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_758.class})
public class MixinBackgroundRenderer {
   @Inject(
      method = {"method_42588(Lnet/minecraft/class_1297;F)Lnet/minecraft/class_758$class_7286;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onGetFogModifier(class_1297 entity, float tickDelta, CallbackInfoReturnable<Object> info) {
      if (Southside.get().getModuleManager().get(NoRender.class).isEnabled() && Southside.get().getModuleManager().get(NoRender.class).blindness.getValue()) {
         info.setReturnValue(null);
      }
   }
}
