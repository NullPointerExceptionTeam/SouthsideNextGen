package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.render.Glow;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class MixinEntityGlow {
   @Inject(
      method = {"isGlowing()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void southside$useCustomGlow(CallbackInfoReturnable<Boolean> cir) {
      Entity self = (Entity)(Object)this;
      if (GodItemDetector.hasTotemPopped(self)) {
         cir.setReturnValue(true);
         cir.cancel();
      } else {
         Glow glow;
         try {
            glow = Southside.INSTANCE.getModuleManager().get(Glow.class);
         } catch (Exception var5) {
            return;
         }

         if (glow != null && glow.shouldGlow(self)) {
            cir.setReturnValue(true);
            cir.cancel();
         }
      }
   }
}
