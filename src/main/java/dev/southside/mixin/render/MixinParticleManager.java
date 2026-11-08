package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.class_691;
import net.minecraft.class_702;
import net.minecraft.class_703;
import net.minecraft.class_727;
import net.minecraft.class_677.class_678;
import net.minecraft.class_677.class_681;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_702.class})
public class MixinParticleManager {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_3058(Lnet/minecraft/class_703;)V"},
      cancellable = true
   )
   public void addParticleHook(class_703 p, CallbackInfo e) {
      NoRender noRender = Southside.get().getModuleManager().get(NoRender.class);
      if (noRender.isEnabled()) {
         if (noRender.explosions.getValue() && p instanceof class_691) {
            e.cancel();
         }

         if (noRender.breakParticles.getValue() && p instanceof class_727) {
            e.cancel();
         }

         if (noRender.fireworks.getValue() && (p instanceof class_681 || p instanceof class_678)) {
            e.cancel();
         }
      }
   }
}
