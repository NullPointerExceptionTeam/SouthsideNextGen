package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.render.Glow;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1297.class})
public class MixinEntityGlowColor {
   @Inject(
      method = {"method_22861()I"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void southside$customGlowColor(CallbackInfoReturnable<Integer> cir) {
      class_1297 self = (class_1297)(Object)this;
      if (GodItemDetector.hasTotemPopped(self)) {
         cir.setReturnValue(-65536);
      } else {
         Glow glow;
         try {
            glow = Southside.INSTANCE.getModuleManager().get(Glow.class);
         } catch (Exception var5) {
            return;
         }

         if (glow != null) {
            if (glow.shouldGlow(self)) {
               cir.setReturnValue(glow.resolveOutlineColor(self));
            }
         }
      }
   }
}
